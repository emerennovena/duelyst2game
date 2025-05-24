package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.*;
import utils.OrderedCardLoader;

import java.util.*;

public class CardManagement {

    private static List<Card> cardList;
    private static HashMap<Integer, Card> handCardList = new HashMap<>();
    private static int handPosition=1;
    private static int currentMaxCard=0;

    public static void cardInitialize(ActorRef out){
        // load player1's card list
        cardList=OrderedCardLoader.getPlayer1Cards(2);
        for (Card card:cardList) {
            handCardList.put(handPosition,card);
            BasicCommands.drawCard(out, card, handPosition, 0);
            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
            handPosition++;
            currentMaxCard++;
            if (handPosition>3) break;
        }
    }

    public static Card getCardInformation(int position){
        return handCardList.get(position);
    }

    public static void activeCard(ActorRef out,int position){
        Card card=getCardInformation(position);
        BasicCommands.drawCard(out, card, position, 1);
        try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public static void removeCard(ActorRef out,int position){
        BasicCommands.deleteCard(out, position);
        try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
        handCardList.remove(position);
    }

    public static void removeCardFromDeck(ActorRef out){
        if (handPosition<=6){
            boolean flag=false;
            for(int i=1;i<handPosition;i++){
                if(handCardList.containsKey(i)==false){
                    flag=true;
                    Card card=cardList.get(currentMaxCard);
                    BasicCommands.drawCard(out, card, i, 0);
                    try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
                    handCardList.put(i,card);
                    currentMaxCard++;
                }
            }
            if (flag==false){
                Card card=cardList.get(currentMaxCard);
                handCardList.put(handPosition,card);
                BasicCommands.drawCard(out, card, handPosition, 0);
                try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
                currentMaxCard++;
                handPosition++;
            }
        }else{
            // if player already has 6 cards in hand, the card in deck need to be removed
            currentMaxCard++;
        }


    }


}
