package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Thread.AbilityWarThread;

public class Celebrity extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("유명 인사", "Cooldown", 60,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public Celebrity(Player player) {
		super(player, "유명 인사", Rank.D,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 모든 플레이어가 자신의 방향을 바라봅니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					
					if(AbilityWarThread.isGameTaskRunning()) {
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f안녕하세요~ &e" + getPlayer().getName() + "&f이에요!"));
						for(Player p : AbilityWarThread.getGame().getParticipants()) {
							if(!p.equals(getPlayer())) {
								Vector vector = getPlayer().getLocation().toVector().subtract(p.getLocation().toVector());
								p.teleport(p.getLocation().setDirection(vector));
							}
						}
					}
					
					Cool.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {}

	@Override
	public void onRestrictClear() {
		
	}

}
