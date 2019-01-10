package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.CooldownTimer;
import Marlang.AbilityWar.Utils.Messager;

public class EnergyBlocker extends AbilityBase {
	
	boolean Default = true;
	
	public EnergyBlocker() {
		super("에너지 블로커", Rank.A,
				ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 절반으로, 근거리 공격 피해를 두 배로 받거나"),
				ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 두 배로, 근거리 공격 피해를 절반으로 받을 수 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "철괴를 우클릭하면 각각의 피해 정도를 뒤바꿉니다. " + Messager.formatCooldown(30)),
				ChatColor.translateAlternateColorCodes('&', "&f현재 상태 &7: &b원거리 &f절반&7, &a근거리 &f두 배"));
		
		registerTimer(Cool);
	}
	
	CooldownTimer Cool = new CooldownTimer(this, 30);
	
	@Override
	public void ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Default = !Default;
					Player p = getPlayer();
					if(Default) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b원거리 &f절반&7, &a근거리 &f두 배로 변경되었습니다."));
						this.setExplain(
								ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 절반으로, 근거리 공격 피해를 두 배로 받거나"),
								ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 두 배로, 근거리 공격 피해를 절반으로 받을 수 있습니다."),
								ChatColor.translateAlternateColorCodes('&', "철괴를 좌클릭하면 각각의 피해 정도를 뒤바꿉니다. &c쿨타임 &7: &f30초"),
								ChatColor.translateAlternateColorCodes('&', "&f현재 상태 &7: &b원거리 &f절반&7, &a근거리 &f두 배"));
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b원거리 &f두 배&7, &a근거리 &f절반으로 변경되었습니다."));
						this.setExplain(
								ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 절반으로, 근거리 공격 피해를 두 배로 받거나"),
								ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 두 배로, 근거리 공격 피해를 절반으로 받을 수 있습니다."),
								ChatColor.translateAlternateColorCodes('&', "철괴를 좌클릭하면 각각의 피해 정도를 뒤바꿉니다. &c쿨타임 &7: &f30초"),
								ChatColor.translateAlternateColorCodes('&', "&f현재 상태 &7: &b원거리 &f두 배&7, &a근거리 &f절반"));
					}
					
					Cool.StartTimer();
				}
			}
		}
	}
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if(e.getEntity() instanceof Player) {
				Player p = (Player) e.getEntity();
				if(p.equals(getPlayer())) {
					if(!e.isCancelled()) {
						DamageCause dc = e.getCause();
						if(dc != null) {
							if(dc.equals(DamageCause.PROJECTILE)) {
								if(Default) {
									e.setDamage(e.getDamage() / 2);
								} else {
									e.setDamage(e.getDamage() * 2);
								}
							} else if(dc.equals(DamageCause.ENTITY_ATTACK)) {
								if(Default) {
									e.setDamage(e.getDamage() * 2);
								} else {
									e.setDamage(e.getDamage() / 2);
								}
							}
						}
					}
				}
			}
		}
	}
	
}
