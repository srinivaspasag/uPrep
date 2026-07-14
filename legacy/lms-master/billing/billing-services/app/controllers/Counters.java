package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.billing.pojos.requests.IncrementCounterReq;
import com.vedantu.commons.daos.CounterDAO;
import com.vedantu.user.managers.UserManager;

public class Counters extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(UserManager.class);

    public static Result increment() {

        long value = CounterDAO.INSTANCE.getNextSequence("invoices", "userId");
        LOGGER.info("Value " + value);
        return ok();
    }

    public static Result incrementCounter() {

        Form<IncrementCounterReq> requestForm = Form.form(IncrementCounterReq.class)
                .bindFromRequest();
        IncrementCounterReq request = requestForm.get();
        long value = CounterDAO.INSTANCE.getNextSequence(request.collectionName, request.fieldName,
                request.byValue);
        LOGGER.info("Value " + value);
        return ok();
    }
}
