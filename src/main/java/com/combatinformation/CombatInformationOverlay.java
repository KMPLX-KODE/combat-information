/*
 * Copyright (c) 2022, KMPLX <github.com/KMPLX-KODE>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.combatinformation;

import javax.inject.Inject;
import java.awt.*;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.util.ColorUtil;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class CombatInformationOverlay extends OverlayPanel
{
    private final Client client;
    private final com.combatinformation.CombatInformationConfig config;
    private final com.combatinformation.CombatInformationPlugin plugin;

    @Inject
    private CombatInformationOverlay(Client client, CombatInformationConfig config, CombatInformationPlugin plugin)
    {
        super(plugin);
        this.plugin = plugin;
        this.client = client;
        this.config = config;

        setPosition(OverlayPosition.BOTTOM_LEFT);
        setPriority(OverlayPriority.MED);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Combat information overlay."));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Combat skills will always be displayed.
        setOverlayText(Skill.ATTACK);
        setOverlayText(Skill.STRENGTH);
        setOverlayText(Skill.DEFENCE);
        setOverlayText(Skill.RANGED);
        setOverlayText(Skill.MAGIC);

        // Add attack style text to overlay.
        setAttackStyleText();

        return super.render(graphics);
    }

    private void setOverlayText(Skill skill)
    {
        final int boosted = client.getBoostedSkillLevel(skill);
        final int base = client.getRealSkillLevel(skill);
        final int boost = boosted - base;
        final Color strColor = getTextColor(boost);
        String str;

        str = ColorUtil.prependColorTag(Integer.toString(boosted), strColor);

        panelComponent.getChildren().add(LineComponent.builder()
                .left(skill.getName())
                .right(str)
                .rightColor(strColor)
                .build());
    }

    private void setAttackStyleText()
    {
        // Always display attack style.
        final AttackStyle attackStyle = plugin.getAttackStyle();
        final String attackStyleString = attackStyle.getName();

        panelComponent.getChildren().add(LineComponent.builder()
                .left(attackStyleString)
                .leftColor(Color.ORANGE)
                .right(plugin.getAutoRetaliateText()) // Display auto-retaliate setting.
                .rightColor(Color.ORANGE)
                .build());
    }

    private Color getTextColor(int boost)
    {
        if (boost < 0)
        {
            return new Color(238, 51, 51);
        }

        return boost <= 0 ? Color.WHITE : Color.GREEN;
    }
}
