package com.dominion;

import com.dominion.card.Card;
import com.dominion.card.SelectionCard;
import com.dominion.card.Decision;

public interface DominionGUI {
	public enum SelectionType {trash, discard, undraw}
	public void setupCardSelection(int upperLimit, boolean exact, SelectionType type, SelectionCard c);
	public void setupGainCard(int upperLimit, boolean exact, SelectionCard c, String s);

	public <E extends Enum<E>> void makeMultipleChoiceDecision(String message, Class<E> enumType, Card c);

	public void trashCardSelection(int playerNum, Decision.CardListDecision cld);
	public void trashCardFromHand(int playerNum, Card c);
	public void trashCardFromPlay(int playerNum, Card c);
	public void addCardToHand(int playerNum, Card c);
	public void discardCard(int playerNum, Card c);
	public void undrawCard(int playerNum, Card c);

	public String getPlayerName(int playerNum);
	public int getLocalPlayer();
}
