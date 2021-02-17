package com.dominion.card;

import com.dominion.ServerTurn;

public interface InteractingCard extends ActionCard {
	//This is called on all of the OTHER players when someone plays the dominion.card
	public Decision reactToCard(ServerTurn turn);
}
