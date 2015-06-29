package edu.hawaii.mirMark.ui.client.widgets;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.scales.LinearScale;
import com.github.gwtd3.api.svg.Axis;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;

import java.util.Random;

/**
 * Created by xzhu on 6/5/15.
 */
public class SiteLevelIllustration {
    private static final int MARGIN = 100;
    private static final int BLOCK_HEIGHT = 4;
    private static final int CANVAS_HEIGHT = 80;
    private static final int AXIS_Y_OFFSET = 30;
    private static final int BLOCK_Y_OFFSET = 1;
    private final int utrStart;
    private final int utrEnd;
    private Random random = new Random();

    private Selection siteBlock = null;
    private LinearScale genomeScale = null;
    private Axis genomeAxis = null;
    private Selection genomeAxisG;

    private final Selection canvas;

    NumberFormat formatter = NumberFormat.getFormat("0.00");

    // rootWidget is the widget it binds itself to
    public SiteLevelIllustration(Widget rootWidget, int utrStart, int utrEnd, String chr) {
        this.utrStart = utrStart;
        this.utrEnd = utrEnd;

        canvas = D3.select(rootWidget)
                .append("svg:svg")
                .classed("site-level-illustration", true)
                .attr("height", 80)
                .attr("width", "100%");

        genomeScale = D3.scale.linear().domain(utrStart, utrEnd).range(MARGIN, 1140 - MARGIN);

        genomeAxis = D3.svg().axis().scale(genomeScale).tickSize(1);

        if (genomeAxisG != null) genomeAxisG.remove();
        genomeAxisG = canvas.append("svg:g")
                .attr("transform", "translate(0," + Integer.toString(CANVAS_HEIGHT - AXIS_Y_OFFSET) + ")")
                .call(genomeAxis);

        canvas.append("svg:text")
                .text(chr)
                .attr("x", MARGIN / 2)
                .attr("y", CANVAS_HEIGHT / 2);
    }

    public void drawSite(String mirName, int relativeStart, int relativeEnd, float probability) {
        int siteStart = relativeStart + utrStart;
        int siteEnd = relativeEnd + utrStart;

        if (siteEnd > utrEnd) {
            MyNotify.notify("Cannot draw the site: The end is out of domain.", NotifyType.DANGER);
            return;
        }

        siteBlock = canvas.append("svg:rect")
                .attr("y", CANVAS_HEIGHT - AXIS_Y_OFFSET - BLOCK_Y_OFFSET - BLOCK_HEIGHT - random.nextDouble() * 16)
                .attr("height", BLOCK_HEIGHT)
                .attr("x", genomeScale.apply(siteStart).asString())
                .attr("width", Double.toString(genomeScale.apply(siteEnd).asDouble() - genomeScale.apply(siteStart).asDouble()))
                .style("fill", "#591613")
                .style("fill-opacity", "0.3");

        siteBlock.append("title")
                .text("miR Name = " + mirName + "; Probability = " + formatter.format(probability));
    }
}
