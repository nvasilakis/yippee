package com.yippee.util;

import com.yippee.crawler.Araneae;
import com.yippee.crawler.Message;
import com.yippee.crawler.frontier.FrontierFactory;
import com.yippee.crawler.frontier.FrontierType;
import com.yippee.crawler.frontier.URLFrontier;
import com.yippee.db.status.DbStatusCheck;
import com.yippee.indexer.Indexer;
import com.yippee.pastry.PingPongService;
import com.yippee.pastry.YippeeEngine;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * This the entry point of the Yippe engine back-end , providing convenient setup
 * for the whole application. Among others, it creates the Pastry ring, the configuration
 * environment, the crawler, the database environment and more. Most importantly,
 * components are started by private methods, so we can have granular component
 * initialization. For instance, we can start only the crawler or pastry ring or
 * any combination of components.
 * <p/>
 * TODO: We can add a config.properties file to read configuration from there on startup
 */
public class EntryPoint {
    
	/**
     * Create logger in the Log4j hierarchy named by by software component
     */
    static Logger logger = Logger.getLogger(EntryPoint.class);
    /**
     * The command-line arguments used to launch the search engine back-end.
     */
    private String[] arguments;
    /**
     * TODO: THESE NEED TO BE GIVEN DYNAMICALLY -- this is where caution message applies to.
     */
    //10 - 1109, 50 - 1291
    final int NO_OF_THREADS = 10;
    final int SIZE_OF_ROBOTS_CACHE = 512;
    
    /**
     * The default constructor does the minimum of setting up the logger
     * properties for the rest of the components (even if we are going to log
     * failure due to argument error).
     */
    private EntryPoint() {
        // SET logger
        PropertyConfigurator.configure("log/log4j.properties");
    }

    /**
     * Configures application based on arguments, after making sure that args
     * are in expected form. Next, components are started by private methods,
     * so we can have granular component initialization. For instance, we can
     * start only the crawler or pastry ring or any combination of components.
     *
     * @param args the user-provided arguments (usually ant)
     * @return true if everything ok; false o/w
     */
    private boolean configure(String[] args) {
        if (args.length < 4) {
            System.out.println("There are no arguments;");
            System.out.println("please check README file, or run 'ant usage'");
            logger.error("Error: No arguments");
            return false;
        } else {
            arguments = args;
            System.out.println("2012, Yippee!");
            cautionMessage();
            String database = args[4];
            logger.info("Database relative path to project root: " + database );
            Configuration.getInstance().setBerkeleyDBRoot(database);
            return true;
        }
    }

    /**
     * Here we add any todos/caution messages we need to show to the user who
     * launches the application. It is called by the constructor.
     */
    private void cautionMessage() {
        // TODO: THESE ARE SOME OF THE CONFIGURATIONS NEED TO BE DONE
        System.out.println("TODO: \n\t * set Thread number \n\t * set Daemon port");
    }

    /**
     * This method fires up the Distributed Hash Table (DHT) service with the
     * aid of FreePastry. More information can be found in com.yippee.pastry
     * package.
     *
     * @return true if everything ok; false o/w;
     */
    private boolean setUpSubstrate() {
        YippeeEngine yippeeEngine = new YippeeEngine(Integer.parseInt(arguments[0]),
                arguments[1], Integer.parseInt(arguments[2]));
        Configuration.getInstance().setPastryEngine(yippeeEngine);
        PingPongService pingPong = new PingPongService();
        new Thread(pingPong, "Ping Pong Thread").start();
        // sleep for a number of seconds so that when the rest of the services
        // launch, pastry is up and running
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * This method fires up the Crawler service with the required configuration.
     * More information can be found in com.yippee.crawler package.
     *
     * @return true if everything ok; false o/w;
     */
    private boolean setupCrawler(String[] args) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Configuration.getInstance().setRobotsCacheSize(SIZE_OF_ROBOTS_CACHE);
        Configuration.getInstance().setCrawlerThreadNumber(NO_OF_THREADS);
        URLFrontier urlFrontier = FrontierFactory.get(FrontierType.POLITE_SIMPLE);
        if (Configuration.getInstance().getService().contains("P")) {
            Configuration.getInstance().getPastryEngine().setupURLFrontier(urlFrontier);
        }
        boolean success = true;
        // only overwrite database with new seeds iff an overwrite flag was given
        if (args[args.length-1].equals("--overwrite")) {
            logger.warn("Overwrite -- loading frontier from the url feed");
            if ((args.length > 4) && (!args[3].contains("--"))) {
                if (!seed(urlFrontier, args[3])) {
                    success = false;
                }
            } else {
                success = false;
            }
        } else { // load URLFrontier from database
            logger.warn("No overwrite -- loading frontier from the database");
            //urlFrontier.load();
        }
        if (success) {
            if (Configuration.getInstance().getService().contains("P"))
                Configuration.getInstance().getPastryEngine().sendPing();
            Araneae threadPool = new Araneae(urlFrontier);

        } else {
            String error = "FATAL: No file found to load urls from";
            logger.error(error);
            System.out.println(error);
        }
        return success;
    }

    /**
     *
     * @param urlFrontier
     * @param seed
     * @return
     */
    private boolean seed(URLFrontier urlFrontier, String seed) {
//        for ( int i = 0; i < 20; i++) {
//            try {
//                Thread.sleep(1000);
//                System.out.println("Sleeping.." + (20-i));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        File seedFile = new File(seed);
        if (!seedFile.exists()) {
            return false;
        }
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(seed));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            while (scanner.hasNextLine()) {
                String urlString = new StringBuilder(scanner.nextLine()).toString();
                if (urlString.startsWith("#")) continue;
                String aLog = "New URL [" + urlString +"]";
                if (Configuration.getInstance().getService().contains("P")) {
                    URL url = new URL(urlString);
                    Configuration.getInstance().getPastryEngine().sendURL(url);
                } else {
                    Message message = new Message(urlString);
                    if (message.getType() == Message.Type.NEW)
                        urlFrontier.push(message);
                }
                logger.info(aLog);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
        return true;
    }


    /**
     * The entry point for the whole Yippee Engine.
     * <p/>
     * Currently, it takes the following
     * arguments:
     * <p/>
     * 1.   The port number on the local machine to which the Pastry node
     * should bind;
     * 2.   The IP address of the Pastry bootstrap node;
     * 3.   The port number of the Pastry bootstrap node;
     * <p/>
     * TODO: We could add a parameter -CIRPS on which component to start
     *
     * @param args The command line arguments at the order specified above, for
     *             instance 9001 130.91.140.235 9001 4444 DB/db1
     */
    public static void main(String[] args) {
        // No entry point -- just read database status
        if (args[args.length-1].equals("--status")) {
            DbStatusCheck check = new DbStatusCheck(args);
            return;
        }
        // Pring arguments -- previous targets have no args
        p2(args);
        // Create entry point to initialize services
        EntryPoint entryPoint = new EntryPoint();
        if (!entryPoint.configure(args)) return;
        // Start Pastry
        if (args[5].contains("P")) {
            Configuration.getInstance().appendService("P");
            entryPoint.setUpSubstrate();
        }
        // Start crawler independently
        if (args[5].contains("C")) {
            System.out.println("Starting crawler");
            Configuration.getInstance().appendService("C");
            if (!entryPoint.setupCrawler(args)) return;
        }
        // Start indexer independently
        if (args[5].contains("I")) {
            System.out.println("Starting indexer");
            Configuration.getInstance().appendService("I");
            
            Indexer ih = new Indexer();
            ih.makeThreads();
            
          //Sleep to allow other nodes to come up before starting the Indexer thread
            for ( int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(1000);
                    System.out.println("Sleeping.." + (20-i));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            ih.start();
        }
    }

    /**
     * Print out startup variables, before running the node
     * [java] 9000            Boot
     * [java] 158.130.105.157
     * [java] 9000
     * [java] feed.url
     * [java] db/prod
     * [java] -C
     * [java] --overwrite
     *
     * @param args the command line arguemts passed by ant
     */
    private static void p(String[] args) {
        System.out.println("Local port.. " + args[0]);
        System.out.println("Boot ip..... " + args[1]);
        System.out.println("Boot port... " + args[2]);
        System.out.println("Feed file... " + args[3]);
        System.out.println("Database.... " + args[4]);
        System.out.println("Service..... " + args[5]);
        System.out.println("Other....... " + args[6]);
    }

    /**
     * The method is the same as before, just outputs arguments in a horizontal
     * way instead of a vertical one.
     *
     * @param args the command line arguments passed by ant
     */
    private static void p2(String[] args) {
        String output = "";
        System.out.println("| L-port |    Boot IP    | B-port |   Feed   | Database | Services | Other | ");
        output += "|  " + args[0] + "   ";
        output += "" + args[1] + "";
        output += "    " + args[2] + "  ";
        output += " " + args[3] + "   ";
        output += " " + args[4] + " ";
        output += "   " + args[5] + "   ";
        output += "   " + args[6] + "  |";
        System.out.println(output);
    }


}
