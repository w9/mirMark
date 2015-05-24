package edu.hawaii.mirMark.ui.client.widgets;

import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyPlacement;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;

/**
 * Created by xzhu on 5/24/15.
 */
public class MyNotify {
    public static void notify(String msg, NotifyType t) {
        NotifySettings notifySettings = NotifySettings.newSettings();
        notifySettings.setTemplate("<div data-notify=\"container\" class=\"col-xs-11 col-sm-3 alert alert-{0}\" role=\"alert\">\n" +
                "    <button type=\"button\" aria-hidden=\"true\" class=\"close\" data-notify=\"dismiss\">x</button>\n" +
                "    <span data-notify=\"message\">{2}</span>\n" +
                "</div>");
        notifySettings.setPlacement(NotifyPlacement.BOTTOM_CENTER);
        notifySettings.setAnimation(Animation.FADE_IN_UP, Animation.FADE_OUT_DOWN);
        notifySettings.setType(t);

        Notify.notify(msg, notifySettings);
    }
}
