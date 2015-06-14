package edu.hawaii.mirMark.ui.client.widgets;

import com.github.gwtd3.api.Colors;
import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.scales.LinearScale;
import com.github.gwtd3.api.svg.Axis;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by xzhu on 6/5/15.
 */
public class SiteLevelIllustration {
    private static final int MARGIN = 100;
    private static final int BLOCK_HEIGHT = 20;
    private static final int CANVAS_HEIGHT = 80;
    private static final int AXIS_Y_OFFSET = 30;
    private static final int BLOCK_Y_OFFSET = 1;

    private Selection siteBlock = null;
    private LinearScale genomeScale = null;
    private Axis genomeAxis = null;
    private Selection genomeAxisG;

    private final Selection canvas;

    // rootWidget is the widget it binds itself to
    public SiteLevelIllustration(Widget rootWidget) {

        canvas = D3.select(rootWidget)
                .append("svg:svg")
                .classed("site-level-illustration", true)
                .attr("height", 0)
                .attr("width", "100%");
    }

    public void redrawTheSite(int utrStart, int utrEnd, int siteStart, int siteEnd) {
        if (siteStart < utrStart || siteEnd > utrEnd) {
            System.err.println("Cannot draw the site: The start and end are out of domain.");
        }

        genomeScale = D3.scale.linear().domain(utrStart, utrEnd).range(MARGIN, 1140 - MARGIN);

        genomeAxis = D3.svg().axis().scale(genomeScale).tickSize(1);

        if (genomeAxisG != null) genomeAxisG.remove();
        genomeAxisG = canvas.append("svg:g")
                .attr("transform", "translate(0," + Integer.toString(CANVAS_HEIGHT - AXIS_Y_OFFSET) + ")")
                .call(genomeAxis);

        if (siteBlock != null) siteBlock.remove();
        siteBlock = canvas.append("svg:rect")
                .attr("y", CANVAS_HEIGHT - AXIS_Y_OFFSET - BLOCK_Y_OFFSET - BLOCK_HEIGHT)
                .attr("height", BLOCK_HEIGHT)
                .attr("x", genomeScale.apply(siteStart).asString())
                .attr("width", Double.toString(genomeScale.apply(siteEnd).asDouble() - genomeScale.apply(siteStart).asDouble()))
                .style("fill", "#F44336");
    }

    private void createSiteBlock() {
    }

    // fold itself using an animation
    public void hide() {
        canvas.attr("height", 0);
    }

    // show itself using an animation
    public void show() {
        canvas.attr("height", CANVAS_HEIGHT);
    }
}
