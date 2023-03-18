package me.pombaconversor;

import org.bukkit.plugin.java.JavaPlugin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.*;

public final class PombaConversor extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadConfig();
        String ip = getConfig().getString("ip");
        String database = getConfig().getString("database");
        String user = getConfig().getString("user");
        String senha = getConfig().getString("senha");
        try {
            // Connect to the database
            Connection conn = DriverManager.getConnection("jdbc:mysql://"+ip+":3306/"+database, user, senha);

            // Retrieve data from yeconomy.players table
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM `yeconomy.players`");

            // Initialize Jackson ObjectMapper for JSON parsing
            ObjectMapper mapper = new ObjectMapper();

            // Iterate over the result set and convert each record
            while (rs.next()) {
                // Extract data from yeconomy.players table
                String owner = rs.getString("key");
                String json = rs.getString("json");
                JsonNode jsonNode = mapper.readTree(json);
                double balance = jsonNode.get("money").asDouble();

                // Prepare SQL statement for nexteconomy_data table
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `nexteconomy_data` (owner, balance, movimentedBalance, transactionsQuantity, transactions, receiveCoins) VALUES (?, ?, ?, ?, ?, ?)");
                pstmt.setString(1, owner);
                pstmt.setDouble(2, balance);
                pstmt.setInt(3, 0);
                pstmt.setInt(4, 0);
                pstmt.setString(5, "");
                pstmt.setInt(6, 0);

                // Execute SQL statement
                pstmt.executeUpdate();

                // Close prepared statement
                pstmt.close();
            }

            // Close database connections
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("JSON parsing error: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
