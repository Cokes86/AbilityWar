package daybreak.abilitywar.game.list.murdermystery.ability.extra;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.murdermystery.Items;
import daybreak.abilitywar.game.list.murdermystery.MurderMystery;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMSHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

@AbilityManifest(name = "경찰", rank = Rank.SPECIAL, species = Species.HUMAN, explain = {
		"금 우클릭으로 금 8개를 소모해 활과 화살을 얻을 수 있습니다.",
		"금 좌클릭으로 금 3개를 소모해 주변 7칸 이내의",
		"모든 플레이어를 1.5초간 기절시킬 수 있습니다."
})
public class Police extends AbilityBase {

	public Police(Participant participant) {
		super(participant);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			PlayerInventory inventory = getPlayer().getInventory();
			ItemStack two = inventory.getItem(2), three = inventory.getItem(3);
			inventory.clear();
			inventory.setItem(2, two);
			inventory.setItem(3, three);
			getPlayer().getInventory().setHeldItemSlot(0);
			((MurderMystery) getGame()).updateGold(getParticipant());
			NMSHandler.getNMS().sendTitle(getPlayer(), "§e역할§f: §a경찰", "§c머더§f를 제압하세요!", 10, 80, 10);
			new Timer(1) {
				@Override
				protected void run(int count) {
				}

				@Override
				protected void onEnd() {
					NMSHandler.getNMS().clearTitle(getPlayer());
				}
			}.setInitialDelay(TimeUnit.SECONDS, 5).start();
		}
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof Player && getGame().isParticipating(e.getEntity().getUniqueId()) && getPlayer().getInventory().getItemInMainHand().isSimilar(Items.MURDERER_SWORD.getStack())) {
			e.setCancelled(false);
			new BukkitRunnable() {
				@Override
				public void run() {
					((Player) e.getEntity()).setHealth(0);
				}
			}.runTaskLater(AbilityWar.getPlugin(), 2L);
		}
	}

	public boolean hasBow() {
		ItemStack stack = getPlayer().getInventory().getItem(2);
		return stack != null && stack.getType() == Material.BOW;
	}

	public int getArrowCount() {
		ItemStack stack = getPlayer().getInventory().getItem(3);
		if (stack != null && stack.getType() == Material.ARROW) {
			return stack.getAmount();
		} else return 0;
	}

	public boolean addArrow() {
		ItemStack stack = getPlayer().getInventory().getItem(3);
		if (stack != null && stack.getType() == Material.ARROW) {
			if (stack.getAmount() < 64) {
				stack.setAmount(stack.getAmount() + 1);
				getPlayer().getInventory().setItem(3, stack);
				getPlayer().sendMessage("§8+ §f1 화살");
				return true;
			} else return false;
		} else {
			getPlayer().getInventory().setItem(3, new ItemStack(Material.ARROW));
			getPlayer().sendMessage("§8+ §f1 화살");
			return true;
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onInteract(PlayerInteractEvent e) {
		if (Items.isGold(e.getItem())) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				MurderMystery murderMystery = (MurderMystery) getGame();
				if (murderMystery.consumeGold(getParticipant(), 8)) {
					if (!addArrow()) {
						murderMystery.addGold(getParticipant());
					} else {
						if (!hasBow()) {
							getPlayer().getInventory().setItem(2, Items.NORMAL_BOW.getStack());
						}
					}
				}
			} else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
				MurderMystery murderMystery = (MurderMystery) getGame();
				if (murderMystery.consumeGold(getParticipant(), 3)) {
					getPlayer().sendMessage("§6전기 충격 §f능력을 사용했습니다.");
					for (Player player : LocationUtil.getNearbyPlayers(getPlayer(), 7, 7)) {
						Stun.apply(getGame().getParticipant(player), TimeUnit.TICKS, 30);
						player.sendMessage("§6전기 충격!");
					}
				}
			}
		}
	}

}
