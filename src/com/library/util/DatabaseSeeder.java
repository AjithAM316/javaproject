package com.library.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class DatabaseSeeder {

    private static final String[] authFirst = {"James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda", "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica", "Thomas", "Sarah", "Charles", "Karen"};
    private static final String[] authLast = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"};
    private static final String[] publishers = {"O'Reilly", "Penguin", "HarperCollins", "MIT Press", "Cengage", "Wiley", "Springer", "Oxford University Press", "Cambridge Press", "Vintage Books"};
    private static final String[] desc3 = {
        "Additionally, the book offers a deep dive into highly complex underlying paradigms, shedding light on esoteric mechanics and structural safety. Packed with industry-standard diagrams and thousands of lines of analytical proofs, it leaves absolutely no stone unturned.\n\n",
        "The sheer volume of knowledge contained within these giant pages bridges the massive conceptual gap between abstract academic theory and grueling, high-stakes enterprise deployment environments. You will learn to construct frameworks that survive catastrophic failures and data corruption entirely on their own.\n\n"
    };
    private static final String[] desc4 = {
        "It is a paradigm-shifting magnum opus that will fundamentally alter your entire approach to problem-solving and rigorous structural analysis on a mass scale.",
        "Whether you are a senior operational architect or a dedicated undergraduate academic, this definitive multi-volume masterwork is the absolute ultimate cornerstone for any robust educational library collection."
    };

    public static void seedIfNeeded(Connection conn) {
        try {
            int currentBooks = 0;
            // 1) Evaluate the DB
            try (java.sql.Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM books")) {
                if (rs.next()) {
                    currentBooks = rs.getInt(1);
                }
            }

            // 2) Force EVERY existing description to be massive if it is small!
            try (java.sql.Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT isbn, genre, description FROM books")) {
                while (rs.next()) {
                    String isbn = rs.getString("isbn");
                    String genre = rs.getString("genre");
                    String desc = rs.getString("description");
                    if (desc == null || desc.length() < 250) {
                        // Needs mega upgrade
                        String newDesc = generateMassiveDescription(genre, new Random(isbn.hashCode()));
                        try (PreparedStatement uSt = conn.prepareStatement("UPDATE books SET description = ? WHERE isbn = ?")) {
                            uSt.setString(1, newDesc);
                            uSt.setString(2, isbn);
                            uSt.executeUpdate();
                        }
                    }
                }
            }

            // 3) Append books up to 100
            int needed = 100 - currentBooks;
            if (needed <= 0) return;

            System.out.println("[DB] Procedurally generating " + needed + " massive textbooks... This will take a second.");

            String sql = "INSERT IGNORE INTO books (isbn, title, author, publisher, genre, year_published, description, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                Random rand = new Random(System.currentTimeMillis());
                
                String[] categories = {"Computer Science", "History", "Politics", "Maths", "Physics"};
                int catIndex = 0;

                for (int i = 0; i < needed; i++) {
                    String genre = categories[catIndex];
                    catIndex = (catIndex + 1) % categories.length;

                    String isbn = "978-X-" + rand.nextInt(90000) + "-" + i;
                    String author = authFirst[rand.nextInt(authFirst.length)] + " " + authLast[rand.nextInt(authLast.length)];
                    String pub = publishers[rand.nextInt(publishers.length)];
                    int year = 1950 + rand.nextInt(74); // 1950 - 2023
                    int copies = 2 + rand.nextInt(8); // 2 - 9
                    
                    String title = generateTitle(genre, rand);
                    String desc = generateMassiveDescription(genre, rand);

                    pst.setString(1, isbn);
                    pst.setString(2, title);
                    pst.setString(3, author);
                    pst.setString(4, pub);
                    pst.setString(5, genre);
                    pst.setInt(6, year);
                    pst.setString(7, desc);
                    pst.setInt(8, copies);
                    pst.setInt(9, copies);
                    pst.addBatch();
                }
                pst.executeBatch();
            }
            System.out.println("[DB] Massive 100-Book library expansion complete!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateTitle(String genre, Random rand) {
        if (genre.equals("Computer Science")) {
            String[] p = {"Advanced", "Mastering", "The Art of", "Foundations of", "Practical", "Applied", "Introduction to", "Algorithms and"};
            String[] t = {"Machine Learning", "Cloud Computing", "Cryptography", "Operating Systems", "Microservices", "Neural Networks", "Data Structures", "Artificial Intelligence", "Compiler Design"};
            return p[rand.nextInt(p.length)] + " " + t[rand.nextInt(t.length)];
        }
        if (genre.equals("History")) {
            String[] p = {"The History of", "Rise and Fall of", "Chronicles of", "Echoes of", "Tragedy of", "Triumph of", "A People's Guide to"};
            String[] t = {"the Roman Empire", "Ancient Mesopotamia", "the Silk Road", "the Industrial Revolution", "the Byzantine World", "Feudal Japan", "the Cold War"};
            return p[rand.nextInt(p.length)] + " " + t[rand.nextInt(t.length)];
        }
        if (genre.equals("Politics")) {
            String[] p = {"The Politics of", "Understanding", "The Future of", "Philosophy of", "Power and", "Anatomy of", "Critique of"};
            String[] t = {"Democracy", "Totalitarianism", "International Relations", "Public Policy", "Political Economy", "Justice", "Geopolitics"};
            return p[rand.nextInt(p.length)] + " " + t[rand.nextInt(t.length)];
        }
        if (genre.equals("Maths")) {
            String[] p = {"Abstract", "Applied", "Advanced", "Theoretical", "A First Course in", "Fundamentals of", "Modern"};
            String[] t = {"Calculus", "Linear Algebra", "Differential Geometry", "Topology", "Number Theory", "Probability Theory", "Combinatorics"};
            return p[rand.nextInt(p.length)] + " " + t[rand.nextInt(t.length)];
        }
        if (genre.equals("Physics")) {
            String[] p = {"Quantum", "Theoretical", "Applied", "Introduction to", "Modern", "Classical", "Advanced"};
            String[] t = {"Mechanics", "Electrodynamics", "Thermodynamics", "Relativity", "Astrophysics", "Particle Physics", "String Theory"};
            return p[rand.nextInt(p.length)] + " " + t[rand.nextInt(t.length)];
        }
        return "Unknown Study of " + genre;
    }

    private static String generateMassiveDescription(String genre, Random rand) {
        String p1 = "", p2 = "";
        if (genre.equals("Computer Science")) {
            String[] chunks1 = {"This exhaustive masterclass provides a deeply intricate exploration into the fabric of modern computing. ", "An indispensable, monolithic guide to the mathematical and practical frameworks that drive today's software systems. "};
            String[] chunks2 = {"Readers will embark on a multi-faceted journey through complex synchronization paradigms, memory allocation algorithms, and high-performance compilation techniques. The text rigorously deconstructs legacy architectural failures while presenting highly resilient, scalable patterns adopted by the world's most elite engineering teams.\n\n", "Delving past the superficial layers of syntax, the authors rigorously prove the theoretical limits of distributed computing. From deep learning pipeline optimization to robust cryptographic implementations, every single chapter is densely packed with peer-reviewed research and production-grade implementation strategies.\n\n"};
            p1 = chunks1[rand.nextInt(chunks1.length)];
            p2 = chunks2[rand.nextInt(chunks2.length)];
        } else if (genre.equals("History")) {
            String[] chunks1 = {"This monumental historical analysis sweeps across centuries to deliver unparalleled insights into human civilization. ", "A breathtaking and visceral narrative capturing the defining moments that shaped our modern geopolitical landscape. "};
            String[] chunks2 = {"By painstakingly cross-referencing ancient texts, archaeological discoveries, and newly declassified diplomatic cables, the author constructs a vivid tapestry of societal evolution. It explores the fierce socio-economic struggles, the devastating plagues, and the brilliant tactical maneuvers of forgotten commanders who drew the borders of the world.\n\n", "Through immersive storytelling and uncompromising scholarly rigor, the book resurrects fallen empires and breathes life into the peasants, monarchs, and philosophers who walked their streets. Every page is dripping with the political intrigue and cultural revolutions that humanity has weathered.\n\n"};
            p1 = chunks1[rand.nextInt(chunks1.length)];
            p2 = chunks2[rand.nextInt(chunks2.length)];
        } else if (genre.equals("Politics")) {
            String[] chunks1 = {"A profoundly deep theoretical investigation into the mechanisms of power, governance, and institutional authority. ", "This masterwork serves as a stark, unforgiving mirror reflecting the complexities and ethical dilemmas of global statecraft. "};
            String[] chunks2 = {"The text methodically dissects the propaganda models, economic coercions, and legal frameworks deployed by both democratic republics and authoritarian regimes. It navigates the treacherous waters of international treaties, highlighting how subtle shifts in foreign policy yield catastrophic ripples across global supply chains and human rights.\n\n", "Drawing heavily on political philosophy and game theory, the narrative scrutinizes the fragility of electoral mandates. From grassroots mobilization to the silent influence of corporate lobbying, readers are taken on a grueling intellectual marathon through the corrupting nature of absolute power.\n\n"};
            p1 = chunks1[rand.nextInt(chunks1.length)];
            p2 = chunks2[rand.nextInt(chunks2.length)];
        } else if (genre.equals("Maths")) {
            String[] chunks1 = {"An incredibly dense, rigorous mathematical textbook designed to push the intellectual limits of the reader. ", "A sweeping journey through the pure, unadulterated beauty of numerical theory and structural logic. "};
            String[] chunks2 = {"Beginning with axiomatic definitions, the authors aggressively scale up to multi-dimensional manifolds, eigenvalue optimizations, and stochastic processes. With over ten thousand exercises ranging from trivial to unsolved conjectures, this book forces the reader to rethink the very nature of infinity and continuous limits.\n\n", "The sheer density of the mathematical proofs presented here is staggering. Moving gracefully from Euclidean spaces to abstract topological deformations, the text builds an unshakeable bridge between pure geometric philosophy and rigorous algebraic formulation without ever holding the reader's hand.\n\n"};
            p1 = chunks1[rand.nextInt(chunks1.length)];
            p2 = chunks2[rand.nextInt(chunks2.length)];
        } else if (genre.equals("Physics")) {
            String[] chunks1 = {"A seminal, mind-bending exploration into the fundamental laws that dictate the motion and energy of our universe. ", "This colossal volume bridges the microscopic realm of subatomic particles with the immense darkness of cosmic expansion. "};
            String[] chunks2 = {"Armed with heavy tensor calculus and differential equations, the book rips apart classical Newtonian mechanics to introduce the terrifying realities of spacetime curvature. The author carefully reconstructs the Standard Model, explaining quantum entanglement, Hawking radiation, and dark matter anomalies with punishing academic precision.\n\n", "It throws the reader directly into the chaos of chaotic attractors and entropic decay. By dissecting the wave-particle duality and the strict limitations of the speed of light, it forces one to confront the strange, counter-intuitive phenomena that govern everything from black holes to the atomic nucleus.\n\n"};
            p1 = chunks1[rand.nextInt(chunks1.length)];
            p2 = chunks2[rand.nextInt(chunks2.length)];
        } else {
            p1 = "A foundational textbook exploring the core elements of the domain. ";
            p2 = "It meticulously researches and cross-references data arrays to supply students with massive amounts of foundational logic.\n\n";
        }

        String p3 = desc3[rand.nextInt(desc3.length)];
        String p4 = desc4[rand.nextInt(desc4.length)];

        return p1 + p2 + p3 + p4;
    }
}
