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

import com.google.inject.Provides;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import static com.combatinformation.AttackStyle.*;

@Slf4j
@PluginDescriptor(
	name = "Combat Information",
	description = "Combat information overlay for combat stats, attack style, and auto-retaliate setting.",
	tags = {"combat", "boosts", "info", "overlay", "auto-retaliate"}
)
public class CombatInformationPlugin extends Plugin
{
	// Varbits for attack style.
	private int attackStyleVarbit = -1;
	private int equippedWeaponTypeVarbit = -1;
	private int castingModeVarbit = -1;

	// Varbits for auto-retaliate.
	private int autoRetaliateVarbit = -1;

	private AttackStyle attackStyle;
	private String autoRetaliate;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CombatInformationOverlay combatInformationOverlay;

	@Inject
	private CombatInformationConfig config;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(combatInformationOverlay);

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invoke(this::start);
		}
	}

	private void start()
	{
		attackStyleVarbit = client.getVar(VarPlayer.ATTACK_STYLE);
		equippedWeaponTypeVarbit = client.getVar(Varbits.EQUIPPED_WEAPON_TYPE);
		castingModeVarbit = client.getVar(Varbits.DEFENSIVE_CASTING_MODE);
		updateAttackStyle(
				equippedWeaponTypeVarbit,
				attackStyleVarbit,
				castingModeVarbit);

		getAutoRetaliateVarbit();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(combatInformationOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
	}

	@Provides
    CombatInformationConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CombatInformationConfig.class);
	}

	private void attackStyle()
	{
		attackStyleVarbit = client.getVar(VarPlayer.ATTACK_STYLE);
		equippedWeaponTypeVarbit = client.getVar(Varbits.EQUIPPED_WEAPON_TYPE);
		castingModeVarbit = client.getVar(Varbits.DEFENSIVE_CASTING_MODE);
		updateAttackStyle(
				equippedWeaponTypeVarbit,
				attackStyleVarbit,
				castingModeVarbit);
	}

	@Nullable
	public AttackStyle getAttackStyle()
	{
		return attackStyle;
	}

	private void updateAttackStyle(int equippedWeaponType, int attackStyleIndex, int castingMode)
	{
		AttackStyle[] attackStyles = WeaponType.getWeaponType(equippedWeaponType).getAttackStyles();
		if (attackStyleIndex < attackStyles.length)
		{
			attackStyle = attackStyles[attackStyleIndex];
			if (attackStyle == null)
			{
				attackStyle = OTHER;
			}
			else if ((attackStyle == CASTING) && (castingMode == 1))
			{
				attackStyle = DEFENSIVE_CASTING;
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int currentAttackStyleVarbit = client.getVar(VarPlayer.ATTACK_STYLE);
		int currentEquippedWeaponTypeVarbit = client.getVar(Varbits.EQUIPPED_WEAPON_TYPE);
		int currentCastingModeVarbit = client.getVar(Varbits.DEFENSIVE_CASTING_MODE);

		if (attackStyleVarbit != currentAttackStyleVarbit || equippedWeaponTypeVarbit != currentEquippedWeaponTypeVarbit || castingModeVarbit != currentCastingModeVarbit)
		{
			boolean weaponSwitch = currentEquippedWeaponTypeVarbit != equippedWeaponTypeVarbit;

			attackStyleVarbit = currentAttackStyleVarbit;
			equippedWeaponTypeVarbit = currentEquippedWeaponTypeVarbit;
			castingModeVarbit = currentCastingModeVarbit;

			updateAttackStyle(equippedWeaponTypeVarbit, attackStyleVarbit,
					castingModeVarbit);
		}


		int currentAutoRetaliateVarbit = client.getVarpValue(172);

		if (currentAutoRetaliateVarbit != autoRetaliateVarbit)
		{
			getAutoRetaliateVarbit();
		}

	}

	@Nullable
	public void getAutoRetaliateVarbit()
	{
		// Auto-retaliate varbit 172
		autoRetaliateVarbit = client.getVarpValue(172);
	}

	@Nullable
	public String getAutoRetaliateText()
	{
		// This has a value of 0 if you have auto retaliate enabled and a value of 1 if you do not.
		if (autoRetaliateVarbit == 0)
		{
			autoRetaliate = "On";
		}

		else
		{
			autoRetaliate = "Off";
		}

		return autoRetaliate;
	}

}
