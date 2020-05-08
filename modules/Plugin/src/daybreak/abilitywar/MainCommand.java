package daybreak.abilitywar;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.config.ability.wizard.AbilitySettingWizard;
import daybreak.abilitywar.config.wizard.DeathWizard;
import daybreak.abilitywar.config.wizard.GameWizard;
import daybreak.abilitywar.config.wizard.InvincibilityWizard;
import daybreak.abilitywar.config.wizard.KitWizard;
import daybreak.abilitywar.config.wizard.SpawnWizard;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.decorator.TeamGame;
import daybreak.abilitywar.game.manager.gui.AbilityListGUI;
import daybreak.abilitywar.game.manager.gui.BlackListGUI;
import daybreak.abilitywar.game.manager.gui.GameModeGUI;
import daybreak.abilitywar.game.manager.gui.InstallGUI;
import daybreak.abilitywar.game.manager.gui.SpecialThanksGUI;
import daybreak.abilitywar.game.manager.gui.SpectatorGUI;
import daybreak.abilitywar.game.manager.gui.TeamPresetGUI;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.CommandHandler;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.Invincibility;
import daybreak.abilitywar.game.script.AbstractScript;
import daybreak.abilitywar.game.script.ScriptWizard;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.math.NumberUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor, TabCompleter {

	private static final Logger logger = Logger.getLogger(MainCommand.class);

	private final AbilityWar plugin;

	MainCommand(AbilityWar plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] split) {
		if (split.length == 0) {
			sender.sendMessage(new String[]{
					Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
					ChatColor.translateAlternateColorCodes('&', "&e버전 &7: &f" + plugin.getDescription().getVersion()),
					ChatColor.translateAlternateColorCodes('&', "&b개발자 &7: &fDaybreak 새벽"),
					ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &f새벽&7#5908"),
					ChatColor.translateAlternateColorCodes('&', "&3&o/" + label + " help &7&o로 명령어 도움말을 확인하세요.")});
		} else {
			if (split[0].equalsIgnoreCase("help")) {
				if (split.length > 1) {
					if (NumberUtil.isInt(split[1])) {
						sendHelpCommand(sender, label, Integer.parseInt(split[1]));
					} else {
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
					}
				} else {
					sendHelpCommand(sender, label, 1);
				}
			} else if (split[0].equalsIgnoreCase("start")) {
				if (sender.isOp()) {
					if (!GameManager.isGameRunning()) {
						try {
							if (GameManager.startGame(Arrays.copyOfRange(split, 1, split.length))) {
								Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + sender.getName() + "&f님이 게임을 시작시켰습니다."));
							}
						} catch (IllegalArgumentException ex) {
							Messager.sendErrorMessage(sender, ex.getMessage());
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 이미 진행되고 있습니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if (split[0].equalsIgnoreCase("stop")) {
				if (sender.isOp()) {
					if (GameManager.isGameRunning()) {
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + sender.getName() + "&f님이 게임을 중지시켰습니다."));

						GameManager.stopGame();
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if (split[0].equalsIgnoreCase("config")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (p.isOp()) {

						if (split.length > 1) {
							parseConfigCommand(p, label, Arrays.copyOfRange(split, 1, split.length));
						} else {
							sendHelpConfigCommand(p, label, 1);
						}
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if (split[0].equalsIgnoreCase("check")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (GameManager.isGameRunning()) {
						AbstractGame game = GameManager.getGame();
						if (game.isParticipating(p)) {
							Participant participant = game.getParticipant(p);
							if (participant.hasAbility()) {
								p.sendMessage(Formatter.formatAbilityInfo(participant.getAbility()).toArray(new String[0]));
							} else {
								Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c당신에게 능력이 할당되지 않았습니다."));
							}
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c게임에 참가하고 있지 않습니다."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if (split[0].equalsIgnoreCase("yes")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (GameManager.isGameOf(AbilitySelect.Handler.class) && ((AbilitySelect.Handler) GameManager.getGame()).getAbilitySelect() != null) {
						AbstractGame game = GameManager.getGame();
						if (game.isParticipating(p)) {
							Participant participant = game.getParticipant(p);
							AbilitySelect select = ((AbilitySelect.Handler) game).getAbilitySelect();
							if (select.isStarted() && !select.isEnded()) {
								if (select.isSelector(participant)) {
									if (!select.hasDecided(participant)) {
										select.decideAbility(participant);
									} else {
										p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c이미 능력 선택을 마치셨습니다."));
									}
								} else {
									Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c당신은 능력을 선택하고 있지 않습니다."));
								}
							} else {
								Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력을 선택하는 중이 아닙니다."));
							}
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c게임에 참가하고 있지 않습니다."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if (split[0].equalsIgnoreCase("no")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (GameManager.isGameOf(AbilitySelect.Handler.class) && ((AbilitySelect.Handler) GameManager.getGame()).getAbilitySelect() != null) {
						AbstractGame game = GameManager.getGame();
						if (game.isParticipating(p)) {
							Participant participant = game.getParticipant(p);
							AbilitySelect select = ((AbilitySelect.Handler) game).getAbilitySelect();
							if (select.isStarted() && !select.isEnded()) {
								if (select.isSelector(participant)) {
									if (!select.hasDecided(participant)) {
										select.alterAbility(participant);
									} else {
										p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c이미 능력 선택을 마치셨습니다."));
									}
								} else {
									Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c당신은 능력을 선택하고 있지 않습니다."));
								}
							} else {
								Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력을 선택하는 중이 아닙니다."));
							}
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c게임에 참가하고 있지 않습니다."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if (split[0].equalsIgnoreCase("abilities")) {
				if (sender instanceof Player) {
					new AbilityListGUI((Player) sender, AbilityWar.getPlugin()).openGUI(1);
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if (split[0].equalsIgnoreCase("skip")) {
				if (sender.isOp()) {
					if (GameManager.isGameOf(AbilitySelect.Handler.class) && ((AbilitySelect.Handler) GameManager.getGame()).getAbilitySelect() != null) {
						AbilitySelect select = ((AbilitySelect.Handler) GameManager.getGame()).getAbilitySelect();
						if (select.isStarted() && !select.isEnded()) {
							select.Skip(sender.getName());
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력을 선택하는 중이 아닙니다."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if (split[0].equalsIgnoreCase("anew")) {
				if (sender.isOp()) {
					if (GameManager.isGameOf(AbilitySelect.Handler.class) && ((AbilitySelect.Handler) GameManager.getGame()).getAbilitySelect() != null) {
						AbilitySelect select = ((AbilitySelect.Handler) GameManager.getGame()).getAbilitySelect();
						select.reset();
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + sender.getName() + "&f님이 모든 참가자의 능력을 재추첨했습니다."));
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if (split[0].equalsIgnoreCase("util")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (p.isOp()) {

						if (split.length > 1) {
							parseUtilCommand(p, label, Arrays.copyOfRange(split, 1, split.length));
						} else {
							sendHelpUtilCommand(p, label, 1);
						}
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if (split[0].equalsIgnoreCase("script")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (p.isOp()) {
						if (split.length > 2) {
							try {
								Class<? extends AbstractScript> scriptClass = ScriptManager.getScriptClass(split[1]);
								if (scriptClass != null) {
									if (Pattern.compile("^[가-힣a-zA-Z0-9_]+$").matcher(split[2]).find() && split[2].length() <= 10) {
										File file = new File("plugins/" + plugin.getName() + "/Script/" + split[2] + ".yml");
										if (!file.exists()) {
											ScriptWizard wizard = new ScriptWizard(p, plugin, scriptClass, split[2]);
											wizard.openScriptWizard(1);
										} else {
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + split[2] + ".yml &f스크립트 파일이 이미 존재합니다."));
										}
									} else {
										p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + split[2] + "&f" + KoreanUtil.getJosa(split[2], Josa.은는) + " 사용할 수 없는 이름입니다."));
									}
								} else {
									Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c존재하지 않는 스크립트 유형입니다."));
								}
							} catch (IllegalArgumentException ex) {
								Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c사용할 수 없는 스크립트 유형입니다."));
								if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
									logger.error(ex.getMessage());
								}
							}
						} else {
							Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "사용법 &7: &f/" + label + " script <유형> <이름>"));
						}
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if (split[0].equalsIgnoreCase("gamemode")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (p.isOp()) {
						GameModeGUI gui = new GameModeGUI(p, plugin);
						gui.openGUI(1);
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if (split[0].equalsIgnoreCase("install")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (p.isOp()) {
						try {
							new InstallGUI(p, plugin, plugin.getInstaller()).openGUI(1);
						} catch (IllegalStateException e) {
							Messager.sendErrorMessage(p, "아직 버전 목록이 불러와지지 않았습니다.");
						}
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if (split[0].equalsIgnoreCase("team")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (GameManager.isGameOf(TeamGame.class)) {
						AbstractGame game = GameManager.getGame();
						if (game.isParticipating(p)) {
							Participant participant = game.getParticipant(p);
							if (split.length > 1) {
								parseTeamCommand(GameManager.getGame(), (TeamGame) GameManager.getGame(), participant, label, Arrays.copyOfRange(split, 1, split.length));
							} else {
								sendHelpTeamCommand(p, label, 1);
							}
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c게임에 참가하고 있지 않습니다."));
						}
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if (split[0].equalsIgnoreCase("specialthanks")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					SpecialThanksGUI gui = new SpecialThanksGUI(p, plugin);
					gui.openGUI(1);
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else {
				Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "존재하지 않는 서브 명령어입니다."));
			}
		}
		return true;
	}

	private void parseConfigCommand(Player p, String label, String[] args) {
		if (args[0].equalsIgnoreCase("kit")) {
			if (args.length > 1) {
				String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
				if (AbilityFactory.isRegistered(name)) {
					new KitWizard(p, plugin, AbilityFactory.getRegistration(AbilityFactory.getByName(name))).Show();
				} else {
					p.sendMessage(ChatColor.RED + name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 능력입니다.");
				}
			} else {
				new KitWizard(p, plugin).Show();
			}
		} else if (args[0].equalsIgnoreCase("spawn")) {
			new SpawnWizard(p, plugin).Show();
		} else if (args[0].equalsIgnoreCase("inv")) {
			new InvincibilityWizard(p, plugin).Show();
		} else if (args[0].equalsIgnoreCase("game")) {
			new GameWizard(p, plugin).Show();
		} else if (args[0].equalsIgnoreCase("death")) {
			new DeathWizard(p, plugin).Show();
		} else if (args[0].equalsIgnoreCase("ability")) {
			new AbilitySettingWizard(p, plugin).openGUI(1);
		} else if (args[0].equalsIgnoreCase("teampreset")) {
			new TeamPresetGUI(p, plugin).openGUI(1);
		} else {
			if (NumberUtil.isInt(args[0])) {
				sendHelpConfigCommand(p, label, Integer.parseInt(args[0]));
			} else {
				Messager.sendErrorMessage(p, "존재하지 않는 콘피그입니다.");
			}
		}
	}

	private void parseTeamCommand(AbstractGame game, TeamGame teamGame, Participant p, String label, String[] args) {
		Player player = p.getPlayer();
		if (args[0].equalsIgnoreCase("chat")) {
			if (p.attributes().TEAM_CHAT.setValue(!p.attributes().TEAM_CHAT.getValue())) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&d팀&5] &f팀 채팅이 비활성화되었습니다."));
			} else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&d팀&5] &f팀 채팅이 활성화되었습니다."));
			}
		} else if (args[0].equalsIgnoreCase("info")) {
			if (teamGame.hasTeam(p)) {
				for (String str : Formatter.formatTeamInfo(teamGame, teamGame.getTeam(p))) {
					player.sendMessage(str);
				}
			} else {
				Messager.sendErrorMessage(player, "팀에 소속되지 않았습니다.");
			}
		} else {
			if (NumberUtil.isInt(args[0])) {
				sendHelpTeamCommand(player, label, Integer.parseInt(args[0]));
			} else {
				Messager.sendErrorMessage(player, "존재하지 않는 팀 명령어입니다.");
			}
		}
	}

	private void parseUtilCommand(Player p, String label, String[] args) {
		if (args[0].equalsIgnoreCase("abi")) {
			if (GameManager.isGameRunning()) {
				if (args.length < 2) {
					Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "사용법 &7: &f/" + label + " util abi <대상>"));
				} else {
					GameManager.getGame().executeCommand(CommandHandler.CommandType.ABI, p, Arrays.copyOfRange(args, 1, args.length), plugin);
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
			}
		} else if (args[0].equalsIgnoreCase("spec")) {
			SpectatorGUI gui = new SpectatorGUI(p, plugin);
			gui.openGUI(1);
		} else if (args[0].equalsIgnoreCase("ablist")) {
			if (GameManager.isGameRunning()) {
				GameManager.getGame().executeCommand(CommandHandler.CommandType.ABLIST, p, Arrays.copyOfRange(args, 1, args.length), plugin);
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
			}
		} else if (args[0].equalsIgnoreCase("blacklist")) {
			BlackListGUI gui = new BlackListGUI(p, plugin);
			gui.openGUI(1);
		} else if (args[0].equalsIgnoreCase("resetcool")) {
			if (GameManager.isGameRunning()) {
				GameManager.getGame().stopTimers(AbilityBase.CooldownTimer.class);
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
						"&f" + p.getName() + "&a님이 플레이어들의 능력 쿨타임을 초기화하였습니다."));
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
			}
		} else if (args[0].equalsIgnoreCase("resetduration")) {
			if (GameManager.isGameRunning()) {
				GameManager.getGame().stopTimers(AbilityBase.DurationTimer.class);
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
						"&f" + p.getName() + "&a님이 플레이어들의 능력 지속시간을 초기화하였습니다."));
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
			}
		} else if (args[0].equalsIgnoreCase("kit")) {
			if (GameManager.isGameOf(DefaultKitHandler.class)) {
				if (args.length < 2) {
					Messager.sendErrorMessage(p,
							ChatColor.translateAlternateColorCodes('&', "사용법 &7: &f/" + label + " util kit <대상>"));
				} else {
					AbstractGame game = GameManager.getGame();
					DefaultKitHandler handler = (DefaultKitHandler) game;
					if (args[1].equalsIgnoreCase("@a")) {
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
								"&f" + p.getName() + "&a님이 &f전체 유저&a에게 기본템을 다시 지급하였습니다."));
						handler.giveDefaultKit(game.getParticipants());
					} else {
						Player target = Bukkit.getPlayerExact(args[1]);
						if (target != null) {
							if (GameManager.getGame().isParticipating(target)) {
								handler.giveDefaultKit(GameManager.getGame().getParticipant(target));
								SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(target);
								Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
										"&f" + p.getName() + "&a님이 &f" + target.getName() + "&a님에게 기본템을 다시 지급하였습니다."));
							} else {
								Messager.sendErrorMessage(p, target.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
							}
						} else {
							Messager.sendErrorMessage(p, args[1] + "은(는) 존재하지 않는 플레이어입니다.");
						}
					}
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않거나 기본 킷을 제공할 수 있는 게임이 아닙니다."));
			}
		} else if (args[0].equalsIgnoreCase("inv")) {
			if (GameManager.isGameOf(Invincibility.Handler.class)) {
				if (GameManager.getGame().isGameStarted()) {
					Invincibility invincibility = ((Invincibility.Handler) GameManager.getGame()).getInvincibility();
					if (invincibility.isInvincible()) {
						invincibility.Stop();
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
								"&f" + p.getName() + "&a님이 무적 상태를 &f비활성화&a하셨습니다."));
					} else {
						if (args.length >= 2) {
							if (NumberUtil.isInt(args[1])) {
								int duration = Integer.parseInt(args[1]);
								invincibility.Start(duration);
								Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
										"&f" + p.getName() + "&a님이 무적 상태를 &f" + TimeUtil.parseTimeAsString(duration) + "&a간 &f활성화&a하셨습니다."));
							} else {
								Messager.sendErrorMessage(p, "시간은 자연수로 입력되어야 합니다.");
							}
						} else {
							invincibility.Start(true);
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
									"&f" + p.getName() + "&a님이 무적 상태를 &f활성화&a하셨습니다."));
						}
					}
				} else {
					Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 시작되지 않았습니다."));
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않거나 무적을 사용할 수 있는 게임이 아닙니다."));
			}
		} else if (args[0].equalsIgnoreCase("team")) {
			if (GameManager.isGameOf(TeamGame.class)) {
				if (args.length > 1) {
					parseTeamUtilCommand(GameManager.getGame(), (TeamGame) GameManager.getGame(), p, label, Arrays.copyOfRange(args, 1, args.length));
				} else {
					sendHelpTeamUtilCommand(p, label, 1);
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다."));
			}
		} else {
			if (NumberUtil.isInt(args[0])) {
				sendHelpUtilCommand(p, label, Integer.parseInt(args[0]));
			} else {
				Messager.sendErrorMessage(p, "존재하지 않는 유틸입니다.");
			}
		}
	}

	private void parseTeamUtilCommand(AbstractGame game, TeamGame teamGame, Player p, String label, String[] args) {
		if (args[0].equalsIgnoreCase("list")) {
			ArrayList<TeamGame.Team> teams = new ArrayList<>(teamGame.getTeams());
			final int maxPage = (teams.size() / 8) + (teams.size() % 8 == 0 ? 0 : 1);
			int page = 1;
			if (args.length >= 2) {
				if (NumberUtil.isInt(args[1])) {
					page = Integer.parseInt(args[1]);
				} else {
					Messager.sendErrorMessage(p, "페이지는 자연수로 입력되어야 합니다.");
				}
			}

			if (page > 0 && page <= maxPage) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6===== &e팀 목록 &f(" + page + "페이지 / " + maxPage + "페이지) &6====="));
				for (int i = (page * 8) - 8; i < (page * 8); i++) {
					if (i >= teams.size()) {
						break;
					}
					TeamGame.Team team = teams.get(i);
					p.sendMessage(ChatColor.YELLOW + "● " + ChatColor.WHITE + team.getName() + ChatColor.WHITE + " (" + team.getDisplayName() + ChatColor.WHITE + ")");
				}
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6========================================"));
			} else {
				Messager.sendErrorMessage(p, "존재하지 않는 페이지입니다.");
			}
		} else if (args[0].equalsIgnoreCase("set")) {
			if (args.length >= 3) {
				Player targetPlayer = Bukkit.getPlayerExact(args[1]);
				if (targetPlayer != null) {
					if (game.isParticipating(targetPlayer)) {
						Participant target = game.getParticipant(targetPlayer);
						if (teamGame.teamExists(args[2])) {
							TeamGame.Team team = teamGame.getTeam(args[2]);
							if (!teamGame.hasTeam(target) || !teamGame.getTeam(target).equals(team)) {
								teamGame.setTeam(target, team);
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + targetPlayer.getName() + "&f님의 팀을 " + team.getName() + " &f(" + team.getDisplayName() + "&f)(으)로 설정하였습니다."));
							} else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + targetPlayer.getName() + "&f님은 이미 " + team.getName() + " &f(" + team.getDisplayName() + "&f) 팀의 일원입니다."));
							}
						} else {
							Messager.sendErrorMessage(p, args[2] + "은(는) 존재하지 않는 팀입니다.");
						}
					} else {
						Messager.sendErrorMessage(p, targetPlayer.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
					}
				} else {
					Messager.sendErrorMessage(p, args[1] + "은(는) 존재하지 않는 플레이어입니다.");
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "사용법 &7: &f/" + label + " util team set <대상> <팀 이름>"));
			}
		} else if (args[0].equalsIgnoreCase("new")) {
			if (args.length >= 3) {
				try {
					TeamGame.Team team = teamGame.newTeam(args[1], ChatColor.translateAlternateColorCodes('&', args[2]));
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님이 " + team.getName() + "&f(" + team.getDisplayName() + "&f) 팀을 새로 만들었습니다."));
				} catch (IllegalStateException | IllegalArgumentException e) {
					Messager.sendErrorMessage(p, e.getMessage());
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "사용법 &7: &f/" + label + " util team new <팀 이름> <팀 별명>"));
			}
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length >= 2) {
				if (teamGame.teamExists(args[1])) {
					TeamGame.Team team = teamGame.getTeam(args[1]);
					teamGame.removeTeam(team);
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님이 " + team.getName() + "&f(" + team.getDisplayName() + "&f) 팀을 삭제했습니다."));
				} else {
					Messager.sendErrorMessage(p, args[1] + "은(는) 존재하지 않는 팀입니다.");
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "사용법 &7: &f/" + label + " util team remove <팀 이름>"));
			}
		} else if (args[0].equalsIgnoreCase("divide")) {
			for (Participant participant : game.getParticipants()) {
				teamGame.setTeam(participant, null);
			}
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님이 모든 플레이어를 각각 하나의 팀으로 나누었습니다."));
		} else {
			if (NumberUtil.isInt(args[0])) {
				sendHelpTeamUtilCommand(p, label, Integer.parseInt(args[0]));
			} else {
				Messager.sendErrorMessage(p, "존재하지 않는 팀 유틸입니다.");
			}
		}
	}

	private void sendHelpCommand(CommandSender sender, String label, int page) {
		int allPage = 2;

		switch (page) {
			case 1:
				sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
						ChatColor.translateAlternateColorCodes('&',
								"&b/" + label + " help <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + page + " 페이지 &7/ &b" + allPage
										+ " 페이지 &7)"),
						Formatter.formatCommand(label, "start", "능력자 전쟁을 시작시킵니다.", true),
						Formatter.formatCommand(label, "stop", "능력자 전쟁을 중지시킵니다.", true),
						Formatter.formatCommand(label, "check", "자신의 능력을 확인합니다.", false),
						Formatter.formatCommand(label, "yes", "자신의 능력을 확정합니다.", false),
						Formatter.formatCommand(label, "no", "자신의 능력을 변경합니다.", false),
						Formatter.formatCommand(label, "abilities", "능력자 전쟁 능력 목록을 확인합니다.", false),
						Formatter.formatCommand(label, "team", "팀 게임의 명령어 목록을 확인합니다.", false),
						Formatter.formatCommand(label, "specialthanks", "능력자 전쟁 플러그인에 기여한 사람들을 확인합니다.", false)});
				break;
			case 2:
				sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
						ChatColor.translateAlternateColorCodes('&',
								"&b/" + label + " help <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + page + " 페이지 &7/ &b" + allPage
										+ " 페이지 &7)"),
						Formatter.formatCommand(label, "skip", "모든 유저의 능력을 강제로 확정합니다.", true),
						Formatter.formatCommand(label, "anew", "모든 유저의 능력을 새로 뽑습니다.", true),
						Formatter.formatCommand(label, "config", "능력자 전쟁 콘피그 명령어를 확인합니다.", true),
						Formatter.formatCommand(label, "util", "능력자 전쟁 유틸 명령어를 확인합니다.", true),
						Formatter.formatCommand(label, "script", "능력자 전쟁 스크립트 편집을 시작합니다.", true),
						Formatter.formatCommand(label, "gamemode", "능력자 전쟁 게임 모드를 설정합니다.", true),
						Formatter.formatCommand(label, "install", "새로운 버전의 다운로드를 시도합니다.", true)});
				break;
			default:
				Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
				break;
		}
	}

	private void sendHelpConfigCommand(CommandSender sender, String label, int page) {
		int allPage = 2;


		switch (page) {
			case 1:
				sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 콘피그"),
						ChatColor.translateAlternateColorCodes('&',
								"&b/" + label + " config <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + page + " 페이지 &7/ &b" + allPage
										+ " 페이지 &7)"),
						Formatter.formatCommand(label + " config", "kit", "능력자 전쟁 기본템을 설정합니다.", true),
						Formatter.formatCommand(label + " config", "kit <능력 이름>", "능력별 기본템을 설정합니다.", true),
						Formatter.formatCommand(label + " config", "spawn", "능력자 전쟁 스폰을 설정합니다.", true),
						Formatter.formatCommand(label + " config", "inv", "초반 무적을 설정합니다.", true),
						Formatter.formatCommand(label + " config", "game", "게임의 전반적인 부분들을 설정합니다.", true),
						Formatter.formatCommand(label + " config", "death", "플레이어 사망에 관련된 콘피그를 설정합니다.", true)});
				break;
			case 2:
				sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 콘피그"),
						ChatColor.translateAlternateColorCodes('&',
								"&b/" + label + " config <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + page + " 페이지 &7/ &b" + allPage
										+ " 페이지 &7)"),
						Formatter.formatCommand(label + " config", "ability", "능력별 설정을 변경합니다.", true),
						Formatter.formatCommand(label + " config", "teampreset", "팀 프리셋 설정 GUI를 엽니다.", true)});
				break;
			default:
				Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
				break;
		}
	}

	private void sendHelpTeamCommand(CommandSender sender, String label, int page) {
		int allPage = 1;

		if (page == 1) {
			sender.sendMessage(new String[]{
					Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 팀 명령어"),
					ChatColor.translateAlternateColorCodes('&', "&b/" + label + " team <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + page + " 페이지 &7/ &b" + allPage + " 페이지 &7)"),
					Formatter.formatCommand(label + " team", "chat", "팀 채팅을 토글합니다.", false),
					Formatter.formatCommand(label + " team", "info", "속해있는 팀 정보를 확인합니다.", false)
			});
		} else {
			Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
		}
	}

	private void sendHelpUtilCommand(CommandSender sender, String label, int page) {
		int allPage = 2;

		switch (page) {
			case 1:
				sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 유틸"),
						ChatColor.translateAlternateColorCodes('&',
								"&b/" + label + " util <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + page + " 페이지 &7/ &b" + allPage
										+ " 페이지 &7)"),
						Formatter.formatCommand(label + " util", "abi <대상/@a>", "대상에게 능력을 임의로 부여합니다.", true),
						Formatter.formatCommand(label + " util", "inv [시간(초)]", "무적 상태를 토글합니다.", true),
						Formatter.formatCommand(label + " util", "spec", "관전자 설정 GUI를 띄웁니다.", true),
						Formatter.formatCommand(label + " util", "ablist", "능력자 목록을 확인합니다.", true),
						Formatter.formatCommand(label + " util", "blacklist", "능력 블랙리스트 설정 GUI를 띄웁니다.", true)});
				break;
			case 2:
				sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 유틸"),
						ChatColor.translateAlternateColorCodes('&',
								"&b/" + label + " util <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + page + " 페이지 &7/ &b" + allPage
										+ " 페이지 &7)"),
						Formatter.formatCommand(label + " util", "resetcool", "플레이어들의 능력 쿨타임을 초기화시킵니다.", true),
						Formatter.formatCommand(label + " util", "resetduration", "플레이어들의 능력 지속시간을 초기화시킵니다.", true),
						Formatter.formatCommand(label + " util", "kit <대상/@a>", "대상에게 기본템을 다시 지급합니다.", true),
						Formatter.formatCommand(label + " util", "team", "팀 유틸 명령어를 확인합니다.", true)});
				break;
			default:
				Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
				break;
		}
	}

	private void sendHelpTeamUtilCommand(CommandSender sender, String label, int page) {
		int allPage = 1;

		if (page == 1) {
			sender.sendMessage(new String[]{
					Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 팀 유틸"),
					ChatColor.translateAlternateColorCodes('&', "&b/" + label + " util team <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + page + " 페이지 &7/ &b" + allPage + " 페이지 &7)"),
					Formatter.formatCommand(label + " util team", "list [페이지]", "팀 목록을 확인합니다.", true),
					Formatter.formatCommand(label + " util team", "set <대상> <팀 이름>", "대상의 팀을 설정합니다.", true),
					Formatter.formatCommand(label + " util team", "new <팀 이름> <팀 별명>", "새로운 팀을 만듭니다.", true),
					Formatter.formatCommand(label + " util team", "remove <팀 이름>", "팀을 삭제합니다.", true),
					Formatter.formatCommand(label + " util team", "divide", "모든 플레이어를 각각 하나의 팀으로 나눕니다.", true)
			});
		} else {
			Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("daybreak/abilitywar") || label.equalsIgnoreCase("ability") || label.equalsIgnoreCase("aw")
				|| label.equalsIgnoreCase("va") || label.equalsIgnoreCase("능력자")) {
			switch (args.length) {
				case 1:
					List<String> subCommands = Messager.asList("start", "stop", "check", "yes", "no", "abilities", "skip", "anew",
							"config", "util", "script", "gamemode", "install", "team", "specialthanks");
					if (args[0].isEmpty()) {
						return subCommands;
					} else {
						subCommands.removeIf(command -> !command.toLowerCase().startsWith(args[0].toLowerCase()));
						return subCommands;
					}
				case 2:
					if (args[0].equalsIgnoreCase("config")) {
						List<String> configs = Messager.asList("kit", "spawn", "inv", "game", "death", "ability", "teampreset");
						if (args[1].isEmpty()) {
							return configs;
						} else {
							configs.removeIf(config -> !config.toLowerCase().startsWith(args[1].toLowerCase()));
							return configs;
						}
					} else if (args[0].equalsIgnoreCase("util")) {
						List<String> utils = Messager.asList("abi", "spec", "ablist", "blacklist", "resetcool",
								"resetduration", "kit", "inv", "team");
						if (args[1].isEmpty()) {
							return utils;
						} else {
							utils.removeIf(util -> !util.toLowerCase().startsWith(args[1].toLowerCase()));
							return utils;
						}
					} else if (args[0].equalsIgnoreCase("script")) {
						List<String> scripts = new ArrayList<>(ScriptManager.getRegisteredScriptNames());
						if (args[1].isEmpty()) {
							return scripts;
						} else {
							scripts.removeIf(script -> !script.toLowerCase().startsWith(args[1].toLowerCase()));
							return scripts;
						}
					} else if (args[0].equalsIgnoreCase("team")) {
						List<String> commands = Messager.asList("chat", "info");
						if (args[1].isEmpty()) {
							return commands;
						} else {
							commands.removeIf(util -> !util.toLowerCase().startsWith(args[1].toLowerCase()));
							return commands;
						}
					}
					break;
				case 3:
					if (args[0].equalsIgnoreCase("util")) {
						if (args[1].equalsIgnoreCase("abi") || args[1].equalsIgnoreCase("kit")) {
							List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
							players.add("@a");
							players.sort(String::compareToIgnoreCase);
							if (args[2].isEmpty()) {
								return players;
							} else {
								players.removeIf(name -> !name.toLowerCase().startsWith(args[2].toLowerCase()));
								return players;
							}
						} else if (args[1].equalsIgnoreCase("team")) {
							List<String> teamUtils = Messager.asList("list", "set", "new", "remove", "divide");
							if (args[2].isEmpty()) {
								return teamUtils;
							} else {
								teamUtils.removeIf(util -> !util.toLowerCase().startsWith(args[2].toLowerCase()));
								return teamUtils;
							}
						}
					}
					break;
				case 4:
					if (args[0].equalsIgnoreCase("util")) {
						if (args[1].equalsIgnoreCase("team")) {
							if (args[2].equalsIgnoreCase("set")) {
								List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
								players.sort(String::compareToIgnoreCase);
								if (args[3].isEmpty()) {
									return players;
								} else {
									players.removeIf(name -> !name.toLowerCase().startsWith(args[3].toLowerCase()));
									return players;
								}
							} else if (args[2].equalsIgnoreCase("remove")) {
								if (GameManager.isGameOf(TeamGame.class)) {
									TeamGame teamGame = (TeamGame) GameManager.getGame();
									return teamGame.getTeams().stream().map(TeamGame.Team::getName).collect(Collectors.toList());
								}
							}
						}
					}
					break;
				case 5:
					if (args[0].equalsIgnoreCase("util")) {
						if (args[1].equalsIgnoreCase("team") && args[2].equalsIgnoreCase("set")) {
							if (GameManager.isGameOf(TeamGame.class)) {
								TeamGame teamGame = (TeamGame) GameManager.getGame();
								return teamGame.getTeams().stream().map(TeamGame.Team::getName).collect(Collectors.toList());
							}
						}
					}
			}
		}

		return Messager.asList();
	}

}
