package com.yan.compiler.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Session {

	private String id;

	public Session(String id, Statement statement) {
		this.id = id;
		this.statement = statement;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	private Statement statement = null;
	private LinkedList<Map<String, Object>> result;
	private Integer count;

	/**
	 * @return the count
	 */
	public final Integer getUpdateCount() {
		return count;
	}

	/**
	 * @return the result
	 */
	public LinkedList<Map<String, Object>> getResult() {
		return result;
	}

	void setStatement(Statement statement) {
		this.statement = statement;
	}

	public boolean query(String sql) throws SQLException {
		count = 0;
		result = null;
		if (statement.execute(sql, Statement.RETURN_GENERATED_KEYS)) {
			ResultSet resultSet = statement.getResultSet();
			LinkedList<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
			ResultSetMetaData metaData = resultSet.getMetaData();
			int len = metaData.getColumnCount();
			while (resultSet.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 1; i <= len; i++) {
					String colName = metaData.getColumnLabel(i);
					Object val = resultSet.getObject(colName);
					map.put(colName, val);
				}
				list.addLast(map);
			}
			result = list;
			count = list.size();

			resultSet.close();
			return true;
		} else {
			count = statement.getUpdateCount();
			if (-1 == count) {
				return false;
			} else {
				return true;
			}
		}
	}

	public void close() throws SQLException {
		statement.close();
		statement = null;
		result = null;
		count = 0;

		ConnManagement m = ConnManagement.factory();
		m.free(id);
	}
}
