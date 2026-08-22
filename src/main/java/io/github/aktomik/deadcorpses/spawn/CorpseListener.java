package io.github.aktomik.deadcorpses.spawn;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CorpseListener implements Listener {

	@EventHandler
	void OnDeath(PlayerDeathEvent event) {
		Location location = event.getEntity().getLocation();
		TagResolver resolvers = TagResolver.resolver(
			Placeholder.parsed("x", Integer.toString(location.getBlockX())),
			Placeholder.parsed("y", Integer.toString(location.getBlockY())),
			Placeholder.parsed("z", Integer.toString(location.getBlockZ()))
		);
		Component message = MiniMessage.miniMessage().deserialize("<blue>you died at <aqua><b><x> <y> <z></b></aqua>", resolvers);
		event.getPlayer().sendMessage(message);
	}
}
