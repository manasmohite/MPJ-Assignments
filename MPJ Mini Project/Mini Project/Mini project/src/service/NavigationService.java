package service;

import dao.FacilityDAO;
import model.Facility;

import java.util.*;

/**
 * Handles campus navigation — search, path finding, building traversal.
 * Basic shortest-path logic using BFS on a campus graph.
 */
public class NavigationService {

    private final FacilityDAO facilityDAO;

    // Adjacency map: building -> list of connected buildings (campus graph)
    private static final Map<String, List<String>> CAMPUS_GRAPH = new LinkedHashMap<>();

    static {
        // Define campus connections (bidirectional)
        addEdge("Main Gate",      "Admin Block");
        addEdge("Admin Block",    "Academic Block A");
        addEdge("Academic Block A", "Academic Block B");
        addEdge("Academic Block B", "Library");
        addEdge("Library",        "Research Center");
        addEdge("Academic Block A", "Cafeteria");
        addEdge("Cafeteria",      "Sports Complex");
        addEdge("Sports Complex", "Hostel Block");
        addEdge("Research Center","Lab Block");
        addEdge("Lab Block",      "Academic Block B");
        addEdge("Admin Block",    "Auditorium");
        addEdge("Auditorium",     "Cafeteria");
    }

    private static void addEdge(String a, String b) {
        CAMPUS_GRAPH.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
        CAMPUS_GRAPH.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
    }

    public NavigationService() {
        facilityDAO = new FacilityDAO();
    }

    /**
     * Search facilities by keyword.
     */
    public List<Facility> searchFacilities(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return facilityDAO.findAll();
        }
        return facilityDAO.search(keyword.trim());
    }

    /**
     * Get all facilities grouped by building.
     */
    public Map<String, List<Facility>> getFacilitiesByBuilding() {
        List<Facility> all = facilityDAO.findAll();
        Map<String, List<Facility>> grouped = new LinkedHashMap<>();
        for (Facility f : all) {
            grouped.computeIfAbsent(f.getBuilding(), k -> new ArrayList<>()).add(f);
        }
        return grouped;
    }

    /**
     * BFS shortest path between two buildings.
     * Returns ordered list of buildings to traverse, or empty if no path.
     */
    public List<String> findShortestPath(String fromBuilding, String toBuilding) {
        if (fromBuilding.equals(toBuilding)) {
            return Collections.singletonList(fromBuilding);
        }

        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(fromBuilding);
        visited.add(fromBuilding);
        parent.put(fromBuilding, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(toBuilding)) {
                return reconstructPath(parent, toBuilding);
            }
            List<String> neighbors = CAMPUS_GRAPH.getOrDefault(current, Collections.emptyList());
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        return Collections.emptyList(); // No path found
    }

    private List<String> reconstructPath(Map<String, String> parent, String target) {
        LinkedList<String> path = new LinkedList<>();
        String current = target;
        while (current != null) {
            path.addFirst(current);
            current = parent.get(current);
        }
        return path;
    }

    /**
     * Format path as human-readable directions.
     */
    public String formatDirections(List<String> path) {
        if (path.isEmpty()) return "No path found between those buildings.";
        if (path.size() == 1) return "You are already at " + path.get(0) + ".";

        StringBuilder sb = new StringBuilder();
        sb.append("🗺️  Route (").append(path.size() - 1).append(" steps):\n\n");
        for (int i = 0; i < path.size(); i++) {
            if (i == 0) {
                sb.append("📍 START: ").append(path.get(i)).append("\n");
            } else if (i == path.size() - 1) {
                sb.append("🏁 DEST:  ").append(path.get(i)).append("\n");
            } else {
                sb.append("   ➡️  ").append(path.get(i)).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Get all distinct building names.
     */
    public List<String> getAllBuildings() {
        return facilityDAO.getBuildings();
    }

    /**
     * Get known buildings from the campus graph.
     */
    public Set<String> getGraphNodes() {
        return CAMPUS_GRAPH.keySet();
    }
}
