import java.util.ArrayList;
import java.util.List;

// Player class
class Player {
    private int id;
    private String name;
    private String sport;
    private int age;
    private String position;

    public Player(int id, String name, String sport, int age, String position) {
        this.id = id;
        this.name = name;
        this.sport = sport;
        this.age = age;
        this.position = position;
    }

    public void display() {
        System.out.println("Player ID: " + id + " | Name: " + name +
                " | Sport: " + sport + " | Age: " + age + " | Position: " + position);
    }
}

// Coach class
class Coach {
    private int id;
    private String name;
    private String sport;
    private int experience;

    public Coach(int id, String name, String sport, int experience) {
        this.id = id;
        this.name = name;
        this.sport = sport;
        this.experience = experience;
    }

    public void display() {
        System.out.println("Coach ID: " + id + " | Name: " + name +
                " | Sport: " + sport + " | Experience: " + experience + " years");
    }
}

// Match class
class Match {
    private int matchId;
    private String team1;
    private String team2;
    private String date;
    private String venue;
    private String result;

    public Match(int matchId, String team1, String team2, String date, String venue, String result) {
        this.matchId = matchId;
        this.team1 = team1;
        this.team2 = team2;
        this.date = date;
        this.venue = venue;
        this.result = result;
    }

    public void display() {
        System.out.println("Match ID: " + matchId + " | " + team1 + " vs " + team2 +
                " | Date: " + date + " | Venue: " + venue + " | Result: " + result);
    }
}

// Sports Management System
public class SportsManagementSystem {
    public static void main(String[] args) {
        List<Player> players = new ArrayList<>();
        List<Coach> coaches = new ArrayList<>();
        List<Match> matches = new ArrayList<>();

        // Add Players
        players.add(new Player(1, "Virat Kohli", "Cricket", 35, "Batsman"));
        players.add(new Player(2, "Rohit Sharma", "Cricket", 36, "Opener"));
        players.add(new Player(3, "Neeraj Chopra", "Athletics", 26, "Javelin Thrower"));
        players.add(new Player(4, "PV Sindhu", "Badminton", 28, "Singles Player"));

        // Add Coaches
        coaches.add(new Coach(1, "Rahul Dravid", "Cricket", 15));
        coaches.add(new Coach(2, "Pullela Gopichand", "Badminton", 20));
        coaches.add(new Coach(3, "Uwe Hohn", "Athletics", 10));

        // Add Matches
        matches.add(new Match(1, "India", "Australia", "2024-11-15", "Melbourne", "India won by 5 wickets"));
        matches.add(new Match(2, "India", "England", "2024-12-01", "Lords", "Draw"));
        matches.add(new Match(3, "India A", "South Africa", "2025-01-10", "Johannesburg", "India A won by 50 runs"));

        // Display Players
        System.out.println("========== PLAYERS ==========");
        for (Player p : players) {
            p.display();
        }

        // Display Coaches
        System.out.println("\n========== COACHES ==========");
        for (Coach c : coaches) {
            c.display();
        }

        // Display Matches
        System.out.println("\n========== MATCHES ==========");
        for (Match m : matches) {
            m.display();
        }

        System.out.println("\nTotal Players: " + players.size());
        System.out.println("Total Coaches: " + coaches.size());
        System.out.println("Total Matches: " + matches.size());
    }
}
