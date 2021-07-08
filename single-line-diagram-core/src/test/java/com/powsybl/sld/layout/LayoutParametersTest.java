/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
public class LayoutParametersTest {

    @Test
    public void test() {
        LayoutParameters layoutParameters = new LayoutParameters()
                .setTranslateX(25)
                .setTranslateY(60)
                .setInitialXBus(10)
                .setInitialYBus(280)
                .setVerticalSpaceBus(20)
                .setHorizontalBusPadding(30)
                .setCellWidth(70)
                .setExternCellHeight(240)
                .setInternCellHeight(50)
                .setStackHeight(40)
                .setShowGrid(true)
                .setShowInternalNodes(true)
                .setScaleFactor(2)
                .setHorizontalSubstationPadding(60)
                .setVerticalSubstationPadding(70)
                .setDrawStraightWires(true)
                .setHorizontalSnakeLinePadding(25)
                .setVerticalSnakeLinePadding(40)
                .setArrowDistance(25)
                .setDiagramName("diag")
                .setAvoidSVGComponentsDuplication(true)
                .setAdaptCellHeightToContent(true)
                .setMaxComponentHeight(10)
                .setMinSpaceBetweenComponents(30)
                .setMinExternCellHeight(150)
                .setAngleLabelShift(42)
                .setLabelCentered(true)
                .setLabelDiagonal(true)
                .setHighlightLineState(false)
                .setTooltipEnabled(true)
                .setAddNodesInfos(true)
                .setMinSpaceForFeederArrows(70)
                .setFeederArrowSymmetry(true);
        LayoutParameters layoutParameters2 = new LayoutParameters(layoutParameters);

        assertEquals(layoutParameters.getTranslateX(), layoutParameters2.getTranslateX(), 0);
        assertEquals(layoutParameters.getTranslateY(), layoutParameters2.getTranslateY(), 0);
        assertEquals(layoutParameters.getInitialXBus(), layoutParameters2.getInitialXBus(), 0);
        assertEquals(layoutParameters.getInitialYBus(), layoutParameters2.getInitialYBus(), 0);
        assertEquals(layoutParameters.getVerticalSpaceBus(), layoutParameters2.getVerticalSpaceBus(), 0);
        assertEquals(layoutParameters.getHorizontalBusPadding(), layoutParameters2.getHorizontalBusPadding(), 0);
        assertEquals(layoutParameters.getCellWidth(), layoutParameters2.getCellWidth(), 0);
        assertEquals(layoutParameters.getExternCellHeight(), layoutParameters2.getExternCellHeight(), 0);
        assertEquals(layoutParameters.getInternCellHeight(), layoutParameters2.getInternCellHeight(), 0);
        assertEquals(layoutParameters.getStackHeight(), layoutParameters2.getStackHeight(), 0);
        assertEquals(layoutParameters.isShowGrid(), layoutParameters2.isShowGrid());
        assertEquals(layoutParameters.isShowInternalNodes(), layoutParameters2.isShowInternalNodes());
        assertEquals(layoutParameters.getScaleFactor(), layoutParameters2.getScaleFactor(), 0);
        assertEquals(layoutParameters.getHorizontalSubstationPadding(), layoutParameters2.getHorizontalSubstationPadding(), 0);
        assertEquals(layoutParameters.getVerticalSubstationPadding(), layoutParameters2.getVerticalSubstationPadding(), 0);
        assertEquals(layoutParameters.isDrawStraightWires(), layoutParameters2.isDrawStraightWires());
        assertEquals(layoutParameters.getHorizontalSnakeLinePadding(), layoutParameters2.getHorizontalSnakeLinePadding(), 0);
        assertEquals(layoutParameters.getVerticalSnakeLinePadding(), layoutParameters2.getVerticalSnakeLinePadding(), 0);
        assertEquals(layoutParameters.getArrowDistance(), layoutParameters2.getArrowDistance(), 0);
        assertEquals(layoutParameters.getDiagramName(), layoutParameters2.getDiagramName());
        assertEquals(layoutParameters.isAvoidSVGComponentsDuplication(), layoutParameters2.isAvoidSVGComponentsDuplication());
        assertEquals(layoutParameters.isAdaptCellHeightToContent(), layoutParameters2.isAdaptCellHeightToContent());
        assertEquals(layoutParameters.getMaxComponentHeight(), layoutParameters2.getMaxComponentHeight(), 0);
        assertEquals(layoutParameters.getMinSpaceBetweenComponents(), layoutParameters2.getMinSpaceBetweenComponents(), 0);
        assertEquals(layoutParameters.getMinExternCellHeight(), layoutParameters2.getMinExternCellHeight(), 0);
        assertEquals(layoutParameters.getAngleLabelShift(), layoutParameters2.getAngleLabelShift(), 0);
        assertEquals(layoutParameters.isLabelCentered(), layoutParameters2.isLabelCentered());
        assertEquals(layoutParameters.isLabelDiagonal(), layoutParameters2.isLabelDiagonal());
        assertEquals(layoutParameters.isHighlightLineState(), layoutParameters2.isHighlightLineState());
        assertEquals(layoutParameters.isTooltipEnabled(), layoutParameters2.isTooltipEnabled());
        assertEquals(layoutParameters.isAddNodesInfos(), layoutParameters2.isAddNodesInfos());
        assertEquals(layoutParameters.getMinSpaceForFeederArrows(), layoutParameters2.getMinSpaceForFeederArrows(), 0);
        assertEquals(layoutParameters.isFeederArrowSymmetry(), layoutParameters2.isFeederArrowSymmetry());
    }
}
