package org.scada_lts.dao.storungsAndAlarms;
/*
 * (c) 2020 hyski.mateusz@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import org.json.JSONArray;
import org.scada_lts.dao.DAO;
import org.springframework.dao.DataAccessException;

import java.util.List;

/**
 * Create by at Mateusz Hyski
 *
 * @author hyski.mateusz@gmail.com
 */
class PointValuesStorungsAndAlarmsDAO {

    private static final String GET_LIVES_SQL = "SELECT " +
            "`id`," +
            "`activation-time`," +
            "`inactivation-time`," +
            "`level`," +
            "`name` FROM apiAlarmsLive LIMIT ? OFFSET ?;";

    public JSONArray getLiveAlarms(int offset,int limit) throws DataAccessException{

        JSONArray jsonArray=new JSONArray();

        List<ApiAlarmsLive> apiAlarmsLives = DAO.getInstance().getJdbcTemp().query(
                GET_LIVES_SQL, new Object[]{limit, offset},
                new ApiAlarmsLiveRowMapper());

        apiAlarmsLives.stream().forEach( alarmsLive -> jsonArray.put(alarmsLive.toJSONObject()));

        return jsonArray;
    }
    public JSONArray getHistoryAlarmsByDateDayAndFilterFromOffset(
            String date_day,
            String filter_with_mysqlrlike,
            int offset,
            int limit) throws DataAccessException {
        JSONArray jsonArray=new JSONArray();
        List<ApiAlarmsHistory> apiAlarmsHistories =  DAO.getInstance().getJdbcTemp().query(

                SqlCommandGenerator.getCommandForHistoryAlarmsByDateDayAndFilterFromOffset(
                        date_day,
                        filter_with_mysqlrlike,
                        offset,
                        limit),

                new ApiAlarmsHistoryRowMapper());

        if ( apiAlarmsHistories.size() != 0) {
            apiAlarmsHistories.stream().forEach( alarmsHistory -> jsonArray.put(alarmsHistory.toJSONObject()));
        }

        return jsonArray;
    }
    public int setAcknowledge(int id) throws DataAccessException {

        return DAO.getInstance().getJdbcTemp().update(SqlCommandGenerator.setAcknowledge(), new Object[] {id});

    }

}
