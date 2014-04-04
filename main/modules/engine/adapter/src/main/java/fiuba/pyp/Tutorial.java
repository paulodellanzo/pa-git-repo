package fiuba.pyp;

import rice.environment.Environment;
import rice.p2p.commonapi.IdFactory;
import rice.p2p.commonapi.rawserialization.RawMessage;
import rice.pastry.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.direct.DirectNodeHandle;
import rice.pastry.direct.DirectPastryNodeFactory;
import rice.pastry.direct.EuclideanNetwork;
import rice.pastry.direct.NetworkSimulator;
import rice.pastry.leafset.LeafSet;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Vector;

public class Tutorial {

    // this will keep track of our applications
    Vector<MyApp> apps = new Vector<MyApp>();

    /**
     * This constructor launches numNodes PastryNodes.  They will bootstrap
     * to an existing ring if one exists at the specified location, otherwise
     * it will start a new ring.
     *
     * @param bindport the local port to bind to
     * @param bootaddress the IP:port of the node to boot from
     * @param numNodes the number of nodes to create in this JVM
     * @param env the environment for these nodes
     * @param useDirect true for the simulator, false for the socket protocol
     */
    public Tutorial(int bindport, InetSocketAddress bootaddress, int numNodes, Environment env, boolean useDirect) throws Exception {

        // Generate the NodeIds Randomly
        NodeIdFactory nidFactory = new RandomNodeIdFactory(env);

        // construct the PastryNodeFactory
        PastryNodeFactory factory;
        if (useDirect) {
            NetworkSimulator<DirectNodeHandle,RawMessage> sim = new EuclideanNetwork<DirectNodeHandle,RawMessage>(env);
            factory = new DirectPastryNodeFactory(nidFactory, sim, env);
        } else {
            factory = new SocketPastryNodeFactory(nidFactory, bindport, env);
        }

        IdFactory idFactory = new PastryIdFactory(env);

        Object bootHandle = null;

        // loop to construct the nodes/apps
        for (int curNode = 0; curNode < numNodes; curNode++) {
            // This will return null if we there is no node at that location

            // construct a node, passing the null boothandle on the first loop will cause the node to start its own ring
            PastryNode node = factory.newNode();

            // construct a new MyApp
            MyApp app = new MyApp(node, idFactory);

            apps.add(app);

            // boot the node
            node.boot(bootHandle);

            if (bootHandle == null) {
                if (useDirect) {
                    bootHandle = node.getLocalHandle();
                } else {
                    // This will return null if we there is no node at that location
                    bootHandle = bootaddress;
                }
            }

            // the node may require sending several messages to fully boot into the ring
            synchronized(node) {
                while(!node.isReady() && !node.joinFailed()) {
                    // delay so we don't busy-wait
                    node.wait(500);

                    // abort if can't join
                    if (node.joinFailed()) {
                        throw new IOException("Could not join the FreePastry ring.  Reason:"+node.joinFailedReason());
                    }
                }
            }

            System.out.println("Finished creating new node "+node);
        }

        // wait 1 second
        env.getTimeSource().sleep(1000);

        // pick a node
        MyApp app = apps.get(numNodes/2);
        PastryNode node = (PastryNode)app.getNode();

        // send directly to my leafset (including myself)
        LeafSet leafSet = node.getLeafSet();

        // pick some node in the leafset
        int i=-leafSet.ccwSize();

        // select the item
        NodeHandle nh = leafSet.get(i);

        // send the message directly to the node
        app.sendMyMsgDirect(nh);

        // wait a bit
        env.getTimeSource().sleep(100);
    }

    /**
     * Usage:
     * java [-cp FreePastry-<version>.jar] rice.tutorial.appsocket.Tutorial localbindport bootIP bootPort numNodes
     *   or
     * java [-cp FreePastry-<version>.jar] rice.tutorial.appsocket.Tutorial -direct numNodes
     *
     * example java rice.tutorial.DistTutorial 9001 pokey.cs.almamater.edu 9001 10
     * example java rice.tutorial.DistTutorial -direct 10
     */
    public static void main(String[] args) throws Exception {
        try {

            boolean useDirect;
            if (args[0].equalsIgnoreCase("-direct")) {
                useDirect = true;
            } else {
                useDirect = false;
            }

            // Loads pastry settings
            Environment env;
            if (useDirect) {
                env = Environment.directEnvironment();
            } else {
                env = new Environment();

                // disable the UPnP setting (in case you are testing this on a NATted LAN)
                env.getParameters().setString("nat_search_policy","never");
            }

            int bindport = 0;
            InetSocketAddress bootaddress = null;

            // the number of nodes to use is always the last param
            int numNodes = Integer.parseInt(args[args.length-1]);

            if (!useDirect) {
                // the port to use locally
                bindport = Integer.parseInt(args[0]);

                // build the bootaddress from the command line args
                InetAddress bootaddr = InetAddress.getByName(args[1]);
                int bootport = Integer.parseInt(args[2]);
                bootaddress = new InetSocketAddress(bootaddr,bootport);
            }

            // launch our node!
            Tutorial dt = new Tutorial(bindport, bootaddress, numNodes, env, useDirect);
        } catch (Exception e) {
            // remind user how to use
            System.out.println("Usage:");
            System.out.println("java [-cp FreePastry-<version>.jar] rice.tutorial.appsocket.Tutorial localbindport bootIP bootPort numNodes");
            System.out.println("  or");
            System.out.println("java [-cp FreePastry-<version>.jar] rice.tutorial.appsocket.Tutorial -direct numNodes");
            System.out.println();
            System.out.println("example java rice.tutorial.DistTutorial 9001 pokey.cs.almamater.edu 9001 10");
            System.out.println("example java rice.tutorial.DistTutorial -direct 10");
            throw e;
        }
    }
}