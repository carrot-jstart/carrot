package org.jstart.carrot.console.infrastructure.pg;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.List;

public class StringListArrayTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<String> strings, JdbcType jdbcType) throws SQLException {
        if (strings != null) {
            Array array = preparedStatement.getConnection().createArrayOf(JdbcType.VARCHAR.name(), strings.toArray(new String[0]));
            preparedStatement.setArray(i, array);
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        Array array = resultSet.getArray(s);
        if (array == null) {
            return null;
        }
        String[] result = (String[]) array.getArray();
        array.free();
        return List.of(result);
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        Array array = resultSet.getArray(i);
        if (array == null) {
            return null;
        }
        String[] result = (String[]) array.getArray();
        array.free();
        return List.of(result);
    }

    @Override
    public List<String> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        Array array = callableStatement.getArray(i);
        if (array == null) {
            return null;
        }
        String[] result = (String[]) array.getArray();
        array.free();
        return List.of(result);
    }
}

