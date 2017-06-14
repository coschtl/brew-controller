package at.dcosta.brew.db;

import static at.dcosta.brew.Configuration.DATABASE_LOCATION;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import at.dcosta.brew.Configuration;
import at.dcosta.brew.xml.dom.Document;
import at.dcosta.brew.xml.dom.DomReader;
import at.dcosta.brew.xml.dom.DomWriter;
import at.dcosta.brew.xml.dom.Element;
import at.dcosta.brew.xml.dom.Text;

public abstract class Database {

	private static class ColumnDefinition {
		private Map<String, Integer> typeMapping;
		private List<Integer> types;

		public ColumnDefinition() {
			typeMapping = new HashMap<>();
			types = new ArrayList<>();
		}

		public void addColumn(String name, int type) {
			Integer typeValue = Integer.valueOf(type);
			typeMapping.put(name, typeValue);
			types.add(typeValue);
		}

		public void addColumn(String name, String typeAsString) {
			addColumn(name, Integer.parseInt(typeAsString));
		}

		public Object convert(int columnId, String value) {
			int type = types.get(columnId - 1);
			switch (type) {
			case Types.BIGINT:
				return new BigInteger(value);
			case Types.DATE:
				return new Date(Long.parseLong(value));
			case Types.DECIMAL:
				return new BigDecimal(value);
			case Types.DOUBLE:
			case Types.REAL:
				return Double.parseDouble(value);
			case Types.FLOAT:
				return Float.parseFloat(value);
			case Types.INTEGER:
				return Integer.parseInt(value);
			case Types.TIMESTAMP:
				return new Timestamp(Long.parseLong(value));
			case Types.VARCHAR:
				return value;
			default:
				throw new IllegalArgumentException("No converter for SQL-Type " + type);
			}
		}
	}

	private static final String SQL_CHECK_TABLE_EXISTS = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?";
	private static final String SQL_LAST_ROW_ID = "SELECT last_insert_rowid()";
	private static final String JDBC_URL_PREFIX = "jdbc:sqlite:";

	public static void importFromXml(File xmlFile) throws IOException, ParserConfigurationException, SAXException {
		DomReader reader = new DomReader();
		Element rootElement = reader.read(xmlFile).getRootElement();
		Iterator<Element> tables = rootElement.getElementIterator();
		while (tables.hasNext()) {
			Element table = tables.next();
			importTable(table);
		}
	}

	private static String getJdbcUrl() {
		return JDBC_URL_PREFIX + Configuration.getInstance().getString(DATABASE_LOCATION);
	}

	private static void importTable(Element table) {
		Element colDef = table.getFirstChild("columnDefinition");
		ColumnDefinition columnDefinition = new ColumnDefinition();
		Iterator<Element> it = colDef.getElementIterator();
		StringBuilder sqlInsert = new StringBuilder(" insert into ").append(table.getAttribute("name")).append(" (");
		StringBuilder sqlParamMarker = new StringBuilder();
		boolean first = true;
		while (it.hasNext()) {
			Element col = it.next();
			String colName = col.getAttribute("name");
			columnDefinition.addColumn(colName, col.getAttribute("type"));
			if (first) {
				first = false;
			} else {
				sqlInsert.append(", ");
				sqlParamMarker.append(", ");
			}
			sqlInsert.append(colName);
			sqlParamMarker.append("?");
		}
		sqlInsert.append(") values (").append(sqlParamMarker).append(")");

		Connection con = getConnection(getJdbcUrl());
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(sqlInsert.toString());
			Element rows = table.getFirstChild("rows");
			Iterator<Element> rowIt = rows.getElementIterator();
			while (rowIt.hasNext()) {
				Element row = rowIt.next();
				Iterator<Element> colIt = row.getElementIterator();
				int i = 1;
				while (colIt.hasNext()) {
					Element col = colIt.next();
					st.setObject(i, columnDefinition.convert(i, col.getText().getTextTrim()));
					i++;
				}
				st.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DatabaseException("Can not import XML file: " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
	}

	protected static void close(Connection con) {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException e) {
			// ignore
		}
	}

	protected static void close(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			// ignore
		}
	}

	protected static void close(Statement st) {
		try {
			if (st != null) {
				st.close();
			}
		} catch (SQLException e) {
			// ignore
		}
	}

	protected static Connection getConnection(String url) {
		try {
			return DriverManager.getConnection(url);
		} catch (SQLException e) {
			throw new DatabaseException("Can not conect ot database: " + e.getMessage(), e);
		}
	}

	private final String jdbcUrl;

	protected Database() {
		jdbcUrl = getJdbcUrl();
		createTablesIfNecessary();
	}

	public void dumpToXml(File outputFile) throws IOException {
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			Document xml = new Document("tables");
			for (String tableName : getTableNames()) {
				Element table = new Element("table").addAttribute("name", tableName);
				xml.addElement(table);
				st = con.prepareStatement("select * from " + tableName);
				rs = st.executeQuery();
				ResultSetMetaData metaData = rs.getMetaData();
				int columnCount = metaData.getColumnCount();
				Element columnDefinition = new Element("columnDefinition");
				table.addChild(columnDefinition);
				for (int i = 1; i <= columnCount; i++) {
					columnDefinition.addChild(new Element("column").addAttribute("name", metaData.getColumnName(i))
							.addAttribute("type", metaData.getColumnType(i)));
				}
				Element rows = new Element("rows");
				table.addChild(rows);
				while (rs.next()) {
					Element row = new Element("row");
					rows.addChild(row);
					for (int i = 1; i <= columnCount; i++) {
						Element column = new Element("column").addChild(new Text(String.valueOf(rs.getObject(i))));
						row.addChild(column);
					}
				}
			}
			DomWriter writer = new DomWriter();
			writer.write(xml, outputFile);
		} catch (SQLException e) {
			throw new DatabaseException("Can not dump " + getClass().getSimpleName() + ": " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
	}

	private void createTablesIfNecessary() {
		String err = "Can not check if table exists: ";
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			boolean createIndex = false;
			String[] createStatements = getCreateTableStatements();
			String[] tableNames = getTableNames();
			if (createStatements.length != tableNames.length) {
				throw new DatabaseException(
						getClass().getSimpleName() + ": ERROR tableNames and create Statements have different size!");
			}
			for (int i = 0; i < tableNames.length; i++) {
				st = con.prepareStatement(SQL_CHECK_TABLE_EXISTS);
				st.setString(1, tableNames[i]);
				rs = st.executeQuery();
				if (rs.next() && rs.getInt(1) == 1) {
					continue;
				}
				close(rs);
				close(st);
				err = "Can not create non existing table: ";
				st = con.prepareStatement(createStatements[i]);
				st.executeUpdate();
				createIndex = true;
				close(st);
			}
			if (createIndex) {
				createStatements = getCreateIndexStatements();
				for (int i = 0; i < createStatements.length; i++) {
					err = "Can not create index: ";
					st = con.prepareStatement(createStatements[i]);
					st.executeUpdate();
					close(st);
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException(getClass().getSimpleName() + ": " + err + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
	}

	protected Connection getConnection() {
		return getConnection(jdbcUrl);
	}

	protected abstract String[] getCreateIndexStatements();

	protected abstract String[] getCreateTableStatements();

	protected int getLastInsertedRowId(Connection con) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_LAST_ROW_ID);
			rs = st.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			throw new DatabaseException("can not get last inserted rowId!");
		} catch (SQLException e) {
			throw new DatabaseException("can not get last inserted rowId: " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
		}
	}

	protected abstract String[] getTableNames();

	protected Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}

	protected Timestamp today() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new Timestamp(cal.getTimeInMillis());
	}

}
