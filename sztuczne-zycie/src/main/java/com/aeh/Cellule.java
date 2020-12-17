package com.aeh;

import java.util.ArrayList;

public class Cellule {
    private int xPos;
    private int yPos;
    private int bacteriaNum;
    private boolean old;
    public ArrayList<Creeper> creepers; //lista pełzaczy w komórce

    public Cellule() {
        creepers = new ArrayList<>();
    }

    public void setXPos(int i) { xPos = i; }
    public void setYPos(int j) { yPos = j; }

    public boolean getOld() { return old; }
    public void setOld(boolean old) { this.old = old; }

    //public void clearAux(){auxList.clear();}
    public int getBactNum() { return bacteriaNum; }

    public void setBactNum(int bactNum) { bacteriaNum = bactNum; }

    public void addBactNum(int bactNum) { bacteriaNum += bactNum; }

    public void reduceBactNum(int bactNum) {
        bacteriaNum -= bactNum;
        if (bacteriaNum < 0) {
            bacteriaNum = 0;
            Init.outStream.println("UWAGA - miała miejsce próba usunięcia z komórki większej liczby bakterii " +
                    "niż w niej się znajdowało");
            //ten komunikat może ułatwić znalezienie błędów powstałych podczas poprawiania algorytmu
            //nie może pojawić się nigdy, gdy symulacja przebiega prawidłowo
        }
    }

    int getCreepersNum() { return creepers.size(); }

    void addCreeper(Creeper newCreeper) { creepers.add(newCreeper); }

    void moveCreeper(Creeper existingCreeper) {
        existingCreeper.setXPos(this.xPos);
        existingCreeper.setYPos(this.yPos);
        creepers.add(existingCreeper);
    }

    void removeCreeper(Creeper creeper) {
        if (!creepers.isEmpty()) {
            creepers.remove(creeper);
        } else {
            Init.outStream.println("UWAGA - miała miejsce próba usunięcia pełzacza z komórki, w której nie ma pełzaczy");
            //ten komunikat może ułatwić znalezienie błędów powstałych podczas poprawiania algorytmu
            //nie może pojawić się nigdy, gdy symulacja przebiega prawidłowo
        }
    }

    void oneCelluleCreepersAction(World w, World tempWorld) {
        for (int i = creepers.size() - 1; i >= 0; i--) //iterowanie od ostatniego do pierwszego pełzacza
            creepers.get(i).creeperAction(w, tempWorld);
    }

    void oneCelluleBacteriaMultiplication(World tempWorld) {
        int newBacteriaNumber, newBacteriaStayingInCell, remainingNewBacteriaMovingToNewCells;
        //nowo urodzone bakterie nie będą mogły być zjedzone w tym takcie, w którym się urodziły
        //ponieważ trafiają do tempWorld. Dzięki temu symualcja jest stabilniejsza.

        if (bacteriaNum > 0) {
            newBacteriaNumber = (int) Math.round (bacteriaNum * Init.BACT_MULTIPLICATION_RATE);
            //liczba nowo urodzonych bakterii

            newBacteriaStayingInCell = (int) (newBacteriaNumber * Init.BACT_SPREAD_RATE);
            tempWorld.board[xPos][yPos].addBactNum(newBacteriaStayingInCell);
            //część nowo urodzonych bakterii określona przez BACT_SPREAD_RATE, która pozostaje
            //w komórce macierzystej (jej odpowiedniku w tempWorld)

            remainingNewBacteriaMovingToNewCells = newBacteriaNumber - newBacteriaStayingInCell;
            //pozostałe nowourodzone bakterie, które maja się przenieść do sąsiednich komórek

            goToNewCellules(tempWorld, remainingNewBacteriaMovingToNewCells);
        }
    }

    private void goToNewCellules(World tempWorld, int remainingNewBacteriaMovingToNewCells) {
        int newXPos, newYPos, newBacteriaPartMovingToNewCell;
        ArrayList<LocationModifier> locationModifiers = Init.initializedLocationModifiersList();

        ArrayList<LocationModifier> existingCellulesLocationModifiers = new ArrayList<>();
        //powyżej lista modyfikatorów położenia dających tylko komórki należące do świata

        for (int i = 0; i < locationModifiers.size(); i++) {
            newXPos = xPos + locationModifiers.get(i).modifyX;
            newYPos = yPos + locationModifiers.get(i).modifyY;

            if (!World.isCelluleOut(newXPos, newYPos))
                existingCellulesLocationModifiers.add(locationModifiers.get(i));
        }

        //losowanie bez powtórzeń
        int i;
        while (existingCellulesLocationModifiers.size() > 0 && remainingNewBacteriaMovingToNewCells > 0) {
            i = (int) (Math.random() * existingCellulesLocationModifiers.size());
            newXPos = xPos + existingCellulesLocationModifiers.get(i).modifyX;
            newYPos = yPos + existingCellulesLocationModifiers.get(i).modifyY;

            if (existingCellulesLocationModifiers.size() == 1) {
                tempWorld.board[newXPos][newYPos].addBactNum(remainingNewBacteriaMovingToNewCells);
                // jeżeli jest to ostatnia komórka, do której moga się przenieść bakterie,
                // które mają się przenieść poza komórkę macierzystą, to przenoszą się do niej wszystkie
                // bakterie, które nie przeniosły się jeszcze do żadnej z sąsiadujących komórek
                remainingNewBacteriaMovingToNewCells = 0;
            } else {
                newBacteriaPartMovingToNewCell = (int) (Math.random() * remainingNewBacteriaMovingToNewCells);
                // losowo określona część nowo urodzonych bakterii, która przeniesie się do nowej komórki
                tempWorld.board[newXPos][newYPos].addBactNum(newBacteriaPartMovingToNewCell);

                remainingNewBacteriaMovingToNewCells -= newBacteriaPartMovingToNewCell;
                // od pozostałych nowo urodzonych bakterii, które mają się przenieść do nowych komórek
                // odejmujemy te, które właśnie przeniosły się do nowej komórki
            }
            existingCellulesLocationModifiers.remove(i);
        }
    }

}
