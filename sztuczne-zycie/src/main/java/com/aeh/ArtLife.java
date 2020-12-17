/* Sztuczne życie v.2.1
   Ostatnia aktualizacja 2017-04-11
   CopyLeft Feliks Kurp 2017
   Cooperation Andrzej Pepłowski (IZ-2/VI)
*/
package com.aeh;


import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.LinkedList;
import java.util.ListIterator;

class Init {
    static final boolean ENABLE_LOGGING = true; //zapisywanie wyników uruchomienia do pliku
    static final int START_INJECTING_AT_TACT = 40; //Rozpoczęcie wtrzyknięcia w takcie
    static final int INJECT_FOR_TACTS = 25; //Długość wstrzyknięcia w taktach
    static final int INJECT_CREEPERS_OFFSET = 10; //Podawanie leczenia przez ... taktów

    static final int INJECTED_BACT_NUM = 64000; //Liczba bakterii do wstrzyknięcia
    static final int INJECTED_CREEPERS_NUM = 2000; //Liczba pełzaczy do wstrzyknięcia

    static final int TACT_TIME_DURATION = 750; //długość symulacji w millisekundach
    static final int MAX_INTENSITY_COUNT = 10000; //maksymalna intensywność koloru przy liczbie


    static final int SIZE_WORLD = 10; //rozmiar świata - NIE ZMIENIAĆ
    static final int NUM_TACT = 500; //całkowita liczba taktów
    static final int VEW_NUM_TACT = 5; //co ile taktów wyświetla wyniki
    static final int START_NUM_CREEPERS = 500; //początkowa liczba pełzaczy
    static final int START_NUM_BACT = 500; //początkowa liczba bakterii
    static final int CREEPER_ENERGY_PRO_LIFE = 3;  //ilość energii potrzebna do urodzenia nowego pełzacza
    static final int CREEPER_INITIAL_ENERGY = 2; //zapas eneergii nowo urodzonego pełzacza
    static final int CREEPER_ENERGY_RESERVE = 2; //rezerwa energii zostawiana podczas rodzenia nowego pełzacza
                                              //potrzebna do przetrwania, gdy jest mało pożywienia
    static final int MAX_CREEPER_NUM_BORN_PER_TACK = 4; //maksymalna liczba pełzaczy rodzonych przez jednego
                                                        //pełzacza w jednym takcie.
    // Liczba pełzaczy, które mogą się urodzić w jednym takcie jest ograniczona przez ilość energii pełzacza po
    // odjęciu CREEPER_ENERGY_RESERVE. Pełzacz nie może zgromadzić zbyt dużo energii, ponieważ gdy tylko
    // przekracza poziom energii równy CREEPER_ENERGY_PRO_LIFE + CREEPER_ENERGY_RESERVE w następnym takcie
    // rodzi co najmniej jednego pełzacza i jego poziom energii jest zmniejszany o CREEPER_ENERGY_PRO_LIFE
    // na każdego urodzonego pełzacza.

    static final int MAX_BACT_EATEN_BY_CREEPER = 13; //Maksymalna liczba bakterii zjadanych przez pełzacza
                                                    //w jednym takcie
    static final double BACT_MULTIPLICATION_RATE = 0.5; //współczynnik rozmnażania bakterii - tyle nowych bakterii
                                                      //powstaje z jednej backterii w każdym takcie.
                                                      //MOŻE PRZYJMOWAĆ WARTOŚCI UŁAMKOWE.
    static final double BACT_SPREAD_RATE = 0.6; //współczynnik rozprzestrzeniania nowo urodzonych bakterii.
                                                //DOPUSZCZALNY ZAKRES: od 0 do 1
                                                //Np. przy wsp. = 0.7, 70% zostaje w komórce,
                                                //w której się urodziła, a 30% przenosi się
                                                //do sąsiednich losowo wybranych
    static final int BACT_NUM_LIMIT = 1000000; //graniczna liczba bakterii dla całego świata.
                                                 //Po przekroczeniu tej liczby komórki umierają/koniec symulacji.

    //lista modyfikatorów pozycji - w niej określamy, które miejsca (względem obecnego położenia)
    //mają być sprawdzane/uwzględniane przy przemieszczaniu się pełzaczy lub bakterii z obecnego położenia
    static ArrayList<LocationModifier> initializedLocationModifiersList() {
        ArrayList<LocationModifier> locationModifiers = new ArrayList<>(4);
        locationModifiers.add(new LocationModifier(-1, 0));
        locationModifiers.add(new LocationModifier(1, 0));
        locationModifiers.add(new LocationModifier(0, -1));
        locationModifiers.add(new LocationModifier(0, 1));

        //możliwość sprawdzania dodatkowo komórek w narożnikach
//        locationModifiers.add(new LocationModifier(-1, -1));
//        locationModifiers.add(new LocationModifier(1, 1));
//        locationModifiers.add(new LocationModifier(-1, 1));
//        locationModifiers.add(new LocationModifier(1, -1));
        return locationModifiers;
    }

    public static PrintStream outStream;
}

class LocationModifier {
    int modifyX, modifyY;

    LocationModifier(int x, int y) {
        modifyX = x;
        modifyY = y;
    }
}

public class ArtLife {
    private static ArrayList<Integer> totallyCreepers = new ArrayList<>();
    private static ArrayList<Integer> totallyBacteria = new ArrayList<>();
    //powyżej kolekcje pomocnicze do zapamiętania liczby bakterii
    //i pełzaczy w każdym takcie, celem późniejszego wyświetlenia

    private static void bacteriaTest(World w) {
        //wyświetla konsolowo liczbę bakterii w danym takcie
        //w układzie tablicy
        for (int i = 0; i < Init.SIZE_WORLD; i++) {
            for (int j = 0; j < Init.SIZE_WORLD; j++)
                Init.outStream.format("%4d", w.board[i][j].getBactNum());
            Init.outStream.print("\n");
        }
    }

    private static void creepersTest(World w) {
        //wyświetla konsolowo liczbę pełzaczy w danym takcie
        //w układzie tablicy
        for (int i = 0; i < Init.SIZE_WORLD; i++) {
            for (int j = 0; j < Init.SIZE_WORLD; j++)
                Init.outStream.format("%4d", w.board[i][j].getCreepersNum());
            Init.outStream.print("\n");
        }
    }

    private static void mainTest(World w) {
        //wyświetla konsolowo w układzie tablicy
        //liczbę bakterii i pełzaczy w danym takcie
        for (int i = 0; i < Init.SIZE_WORLD; i++) {
            for (int j = 0; j < Init.SIZE_WORLD; j++)
                Init.outStream.format("%8d|%-8d", w.board[i][j].getBactNum(),
                        w.board[i][j].getCreepersNum());
            Init.outStream.println();
        }
    }

    private static int totalNum(World w, String what) {
        //zwraca sumaryczną liczbę bakterii, lub pełzaczy, w całym świecie
        int sum = 0;
        for (int i = 0; i < Init.SIZE_WORLD; i++)
            for (int j = 0; j < Init.SIZE_WORLD; j++)
                switch (what) {
                    case "BACTERIA":
                        sum += w.board[i][j].getBactNum();
                        break;
                    case "CREEPERS":
                        sum += w.board[i][j].getCreepersNum();
                }
        return sum;
    }

    private static void addNewBornOrganismsToMainWorldCellules(World mainWorld, World tempWorld) {
        for (int i = 0; i < Init.SIZE_WORLD; i++) {
            for (int j = 0; j < Init.SIZE_WORLD; j++) {
                mainWorld.board[i][j].addBactNum(tempWorld.board[i][j].getBactNum());
                mainWorld.board[i][j].creepers.addAll(tempWorld.board[i][j].creepers);
            }
        }
    }

    private static void printParams() {
        Init.outStream.print("  PARAMETRY URUCHOMIENIA\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Całkowita liczba taktów  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d", Init.NUM_TACT);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Wyświetlaj wyniki co  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d taktów", Init.VEW_NUM_TACT);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Początkowa liczba pełzaczy  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d", Init.START_NUM_CREEPERS);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Początkowa liczba bakterii  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d", Init.START_NUM_BACT);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Liczba energii do urodzenia pełzacza  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d", Init.CREEPER_ENERGY_PRO_LIFE);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Zapas energii nowourodzonego pełzacza  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d", Init.CREEPER_INITIAL_ENERGY);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Rezerwa energii zostawiana po urodz. pełzacza  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d", Init.CREEPER_ENERGY_RESERVE);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Max. pełzaczy w 1 takcie  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d", Init.MAX_CREEPER_NUM_BORN_PER_TACK);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Max. bakterii do zjedz. przez pełzacza w 1 takcie  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d", Init.MAX_BACT_EATEN_BY_CREEPER);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Współczynnik rozmnażania bakterii  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %.2f", Init.BACT_MULTIPLICATION_RATE);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Współczynnik rozprzestrzeniania bakterii  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %.2f", Init.BACT_SPREAD_RATE);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Początek wstrzyknięcia w  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d takcie", Init.START_INJECTING_AT_TACT);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Długość wstrzyknięcia  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d takty/-ów", Init.INJECT_FOR_TACTS);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Wstrzyknięcie pełzaczy przez  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d takty/taktów", Init.INJECT_CREEPERS_OFFSET);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Liczba bakterii do wstrzyknięcia  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d", Init.INJECTED_BACT_NUM);
        Init.outStream.println("\n");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  Liczba pełzaczy do wstrzyknięcia  ");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.format("  %d", Init.INJECTED_CREEPERS_NUM);
        Init.outStream.println("\n\n");

    }

    public static void main(String[] args) {
        try {
            Init.outStream = new PrintStream(System.out, true, "UTF-8");
        }
        catch (UnsupportedEncodingException e){
            Init.outStream = System.out;
            Init.outStream.println("Nieobslugiwane kodowanie.");
        }
        if (Init.ENABLE_LOGGING) {
            File logDir = new File("log/");
            if (!logDir.exists()) {
                logDir.mkdir();
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String timestamp = formatter.format(date).replaceAll("[:-]", "").replaceAll("[ ]", "_");
            Init.outStream.print("Zapisywanie do pliku log/" + timestamp + ".txt");
            try {
                OutputStream printStream = new FileOutputStream("log/" + timestamp + ".txt", true);
                Init.outStream = new PrintStream(printStream, true, "UTF-8");
            } catch (IOException e) {
                Init.outStream.println("Nie mogę zapisać logi");
                e.printStackTrace();
            }
        }
        printParams();

        World mainWorld = new World();
        World tempWorld; // dodatkowy świat potrzebny czasowo w trakcie creepersAndBacteriaAction
                         // do przechowywania nowo urodzonych bakterii (wszystkich) i pełzaczy (tylko tych
                         // które przemieszczają się do sąsiednich komórek), aby nie zwiększały populacji
                         // w niewylosowanych jeszcze komórkach.
                         // Po zakończaniu akcji pełzaczy i bakterii dla wszystkich komórek,
                         // bakterie i pełzacze z tempWorld są dodawane do odpowiednich komórek mainWorld.
                         // Przed każdym creepersAndBacteriaAction wykonywanym dla wszystkich komórek mainWorld,
                         // tworzony jest nowy, pusty tempWorld.

        //--------------------------------------------------------
        //kod do testowania - nie używany w standardowej symulacji
        //--------------------------------------------------------
//        for (int i = 0; i < Init.SIZE_WORLD; i++) {
//            for (int j=0; j < Init.SIZE_WORLD; j++) {
//                mainWorld.setBacteriaNumAtPosition(5, i, j);
//            }
//        }
//        mainWorld.setOneCreeperAtPosition(5, 5);
        //--------------------------------------------------------
        //kod do testowania - nie używany w standardowej symulacji
        //--------------------------------------------------------

        mainWorld.sowBacteries(Init.START_NUM_BACT);
        mainWorld.sowCreepers(Init.START_NUM_CREEPERS);
        int numTact = 0, num, totalBactNum;

        Init.outStream.println("  KOMÓRKI ŚWIATA");
        Init.outStream.println("_____________________________________________________");

        Init.outStream.println("Stan początkowy");
        mainTest(mainWorld);

        totallyCreepers.add(totalNum(mainWorld, "CREEPERS"));
        totallyBacteria.add(totalNum(mainWorld, "BACTERIA"));

        LinkedList<Cellule[][]> worldHistory = new LinkedList<Cellule[][]>();
        worldHistory.add(mainWorld.cloneBoard());

        Point injectionPoint = new Point((int) Math.round((Init.SIZE_WORLD-1)*Math.random()),
                (int) Math.round((Init.SIZE_WORLD-1)*Math.random()));
        int liczbaBakterii = Init.INJECTED_BACT_NUM;
        int liczbaPelzaczy = Init.INJECTED_CREEPERS_NUM / Init.INJECT_FOR_TACTS;

        boolean prematureEndOfSimulation = false;
        while (numTact < Init.NUM_TACT && !prematureEndOfSimulation) {
            num = 0;
            while (num < Init.VEW_NUM_TACT && !prematureEndOfSimulation) {

                //Wstrzyknięcie bakterii
                if (Init.START_INJECTING_AT_TACT <= numTact
                        && numTact < Init.START_INJECTING_AT_TACT+Init.INJECT_FOR_TACTS) {

                    int liczba = liczbaBakterii/((Init.START_INJECTING_AT_TACT+Init.INJECT_FOR_TACTS) - numTact);

                    mainWorld.board[injectionPoint.x][injectionPoint.y].addBactNum(liczba);
                    liczbaBakterii-=liczba;
                }
                //Wstrzyknięcie pełzaczy
                if (Init.START_INJECTING_AT_TACT + Init.INJECT_CREEPERS_OFFSET <= numTact
                        && numTact < Init.START_INJECTING_AT_TACT+Init.INJECT_FOR_TACTS + Init.INJECT_CREEPERS_OFFSET) {

                    for (int i = 0; i < liczbaPelzaczy; i++) {
                        mainWorld.board[injectionPoint.x][injectionPoint.y].addCreeper(new Creeper(injectionPoint.x, injectionPoint.y));
                    }

                }

                tempWorld = new World();

                mainWorld.creepersAndBacteriaAction (mainWorld, tempWorld);
                addNewBornOrganismsToMainWorldCellules (mainWorld, tempWorld);
                totallyCreepers.add(totalNum(mainWorld, "CREEPERS"));
                totalBactNum = totalNum(mainWorld, "BACTERIA");
                totallyBacteria.add(totalBactNum);
                
                if (totalBactNum > Init.BACT_NUM_LIMIT) prematureEndOfSimulation = true;
                
                num++;
                numTact++;
            }

            worldHistory.add(mainWorld.cloneBoard());

            Init.outStream.println("_____________________________________________________");
            Init.outStream.println("Przebieg " + numTact);
            mainTest(mainWorld);
        }

        Init.outStream.println();
        Init.outStream.println("  LICZBA ORGANIZMÓW");
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  BAKTERIE");
        for (int i = 0; i < totallyCreepers.size(); i++) {
            Init.outStream.format("%8d%n",  totallyBacteria.get(i));
            //Init.outStream.println(i + "  " + totallyCreepers.get(i) + "  " + totallyBacteria.get(i));
        }
        Init.outStream.println();
        Init.outStream.println("_____________________________________________________");
        Init.outStream.println("  PEŁZACZE");
        for (int i = 0; i < totallyCreepers.size(); i++) {
            Init.outStream.format("%8d%n", totallyCreepers.get(i));
            //Init.outStream.println(i + "  " + totallyCreepers.get(i) + "  " + totallyBacteria.get(i));
        }
        if (prematureEndOfSimulation) Init.outStream.println("Sumaryczna liczba bakterii przekroczyła "+ Init.BACT_NUM_LIMIT
                + " - komórki umierają/koniec symulacji.");
        Init.outStream.println();

        MainWindow mainWindow = new MainWindow(worldHistory);
    }
}

class TimerTick implements ActionListener {
    private MainWindow parent;
    private int lastId;
    TimerTick(MainWindow parent, int lastId) {
        this.parent = parent;
        this.lastId = lastId;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (!this.parent.jestKoniec()) {
            this.parent.nastepny();
        } else {
            this.parent.stopTimer();
        }
    }
}

class WorldGraphics extends JPanel {
    private Cellule[][] board;
    private int rozmiarKomorki;

    public WorldGraphics(Cellule[][] board, int rozmiarKomorki) {
        this.board = board;
        this.rozmiarKomorki = rozmiarKomorki;
    }

    public void updateState(Cellule[][] board, int rozmiarKomorki) {
        this.board = board;
        this.rozmiarKomorki = rozmiarKomorki;

        this.repaint();
    }
    public void updateState(Cellule[][] board) {
        this.updateState(board, this.rozmiarKomorki);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(196,196,196));
        for (int i = 0; i < Init.SIZE_WORLD; i++) {
            for (int j = 0; j < Init.SIZE_WORLD; j++) {
                Color bactColor = this.getBactColor(this.board[j][i]);
                Color creeperColor = this.getCreeperColor(this.board[j][i]);
                g.setColor(bactColor);
                g.fillRect(i* rozmiarKomorki, j* rozmiarKomorki, rozmiarKomorki, rozmiarKomorki / 2);
                g.setColor(creeperColor);
                g.fillRect(i* rozmiarKomorki, j* rozmiarKomorki + rozmiarKomorki / 2, rozmiarKomorki, rozmiarKomorki / 2);
            }
        }
    }

    private Color getBactColor(Cellule cell) {
        float alpha = (float)(cell.getBactNum()) / (float)(Init.MAX_INTENSITY_COUNT);
        if (alpha > 0) {
            alpha += (float) 0.2;
            if (alpha > 1) alpha = 1;
        }
        return new Color(1, 0, 1, alpha);
    }
    private Color getCreeperColor(Cellule cell) {
        float alpha = (float)(cell.getCreepersNum()) / (float)(Init.MAX_INTENSITY_COUNT);
        if (alpha > 0) {
            alpha += (float) 0.2;
            if (alpha > 1) alpha = 1;
        }
        return new Color(0, 1, 0, alpha);
    }
}

class MainWindow extends JFrame implements ActionListener {
    private WorldGraphics worldGraphics;
    private JLabel biezacyLabel;
    private JButton startButton, stopButton;

    private int windowSize;

    private LinkedList<Cellule[][]> board;
    ListIterator<Cellule[][]> iterator;

    private Cellule[][] biezacy;

    private Timer timer;

    public MainWindow(LinkedList<Cellule[][]> board) {
        this(board, 600);
    }

    public MainWindow(LinkedList<Cellule[][]> board, int windowSize) {
        super("World visualization");

        this.board = board;
        this.initSnapshot();
        this.windowSize = windowSize;
        JPanel controlPanel = new JPanel();
        this.startButton = new JButton("Rozpocznij");
        this.stopButton = new JButton("Stop");
        this.stopButton.setEnabled(false);
        biezacyLabel = new JLabel("Stan poczatkowy");

        startButton.addActionListener(this);
        stopButton.addActionListener(this);
        startButton.setActionCommand("START");
        stopButton.setActionCommand("STOP");
        controlPanel.add(biezacyLabel);
        controlPanel.add(startButton); controlPanel.add(stopButton);
        this.updateState();
        this.add(controlPanel);
        controlPanel.setBounds(0, windowSize - 128, windowSize, windowSize);
        setBackground(new Color(246,246,246));
        setSize(this.windowSize, this.windowSize+128);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        this.timer = new Timer(Init.TACT_TIME_DURATION, new TimerTick(this, this.board.size() - 1));
        this.timer.setInitialDelay(50);

        setVisible(true);
    }

    int getCurrentId() {
        if (this.board != null) {
            return board.indexOf(this.biezacy);
        }
        return -1;
    }

    private void initSnapshot() {
        this.iterator = this.board.listIterator();
        if (this.iterator.hasNext()) {
            this.biezacy = this.iterator.next();
        }
    }

    public boolean jestPoczatek() {
        return this.getCurrentId() == 0;
    }

    public boolean jestKoniec() {
        return this.getCurrentId() == this.board.size() - 1;
    }

    private void poczatek() {
        while (this.iterator.hasPrevious()) {
            this.biezacy = this.iterator.previous();
        }
    }

    public void startTimer() {
        if (!this.jestPoczatek()) {
            this.poczatek();
        }
        if (this.timer != null) {
            this.timer.start();
        }
        this.startButton.setEnabled(false);
        this.stopButton.setEnabled(true);
    }

    public void stopTimer() {
        if (this.timer.isRunning()) {
            this.timer.stop();
            this.startButton.setEnabled(true);
            this.stopButton.setEnabled(false);
        }
    }

    public void nastepny() {
        if (this.iterator.hasNext()) {
            this.biezacy = this.iterator.next();
            this.updateState();
        }
    }

    private void updateState() {
        if (worldGraphics != null) {
            worldGraphics.updateState(this.biezacy);
        } else {
            this.worldGraphics = new WorldGraphics(this.biezacy,this.windowSize/10);
            this.add(worldGraphics);
            worldGraphics.setBounds(0, 0, windowSize, windowSize);
        }

        int currentId = getCurrentId();
        if (!jestPoczatek()) {
            int tactNum = currentId*Init.VEW_NUM_TACT;
            this.biezacyLabel.setText("Takt " + tactNum);
        } else {
            this.biezacyLabel.setText("Stan poczatkowy");
        }
    }


    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "START":
                this.startTimer();
                break;

            case "STOP":
                this.stopTimer();
                break;
        }
    }
}
