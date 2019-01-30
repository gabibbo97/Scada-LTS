package org.scada_lts.mango.service;

import com.serotonin.mango.rt.EventManager;
import com.serotonin.web.i18n.LocalizableMessage;
import org.scada_lts.common.ElementType;
import org.scada_lts.dao.UserLockDAO;
import org.scada_lts.dao.model.userLock.UserLock;
import org.springframework.stereotype.Service;

/**
 * Class created by Arkadiusz Parafiniuk
 *
 * @Author arkadiusz.parafiniuk@gmail.com
 */
@Service
public class UserLockService {

    UserLockDAO userLockDAO = new UserLockDAO();
    EventManager eventManager =  new EventManager();

    public void lockDataPoint(int userId, long dataPointId) {
        UserLock userLock = new UserLock();
        userLock.setUserId(userId);
        userLock.setLockType(ElementType.DATA_POINT);
        userLock.setTypeKey(dataPointId);
        userLock.setTimestamp(System.currentTimeMillis());

        LocalizableMessage message = new LocalizableMessage("event.dp.locked");

//        eventManager.raiseEvent(
//                new UserLockEventType(),
//                System.currentTimeMillis(),
//                false,
//                AlarmLevels.INFORMATION,
//                new LocalizableMessage("event.dp.locked"),
//                new HashMap<>());

        userLockDAO.insertUserLock(userLock);
    }

    public void unlockDataPoint(int userId, long dataPointId) {

//        eventManager.raiseEvent(
//                new UserLockEventType(),
//                System.currentTimeMillis(),
//                false,
//                AlarmLevels.INFORMATION,
//                new LocalizableMessage("event.dp.unlocked"),
//                new HashMap<>());

        userLockDAO.deleteUserLock(ElementType.DATA_POINT, dataPointId);
    }

    public boolean checkIfDataPointIsLocked(long dataPointId) {
        boolean result;
        if (!(userLockDAO.selectUserLock(ElementType.DATA_POINT, dataPointId)==null)){
            result = true;
        } else {
            result = false;
        }
        return result;
    }

}
