package net.william278.husktowns.database;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.ServerWorld;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Database {

    protected final HuskTowns plugin;
    private final String schemaFile;
    private boolean loaded;

    protected Database(@NotNull HuskTowns plugin, @NotNull String schemaFile) {
        this.plugin = plugin;
        this.schemaFile = "database/" + schemaFile;
    }

    /**
     * Get the schema statements from the schema file
     *
     * @return the {@link #format formatted} schema statements
     */
    @NotNull
    protected final String[] getSchema() {
        try (InputStream schemaStream = Objects.requireNonNull(plugin.getResource(schemaFile))) {
            final String schema = new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);
            return format(schema).split(";");
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Failed to load database schema", e);
        }
        return new String[0];
    }

    /**
     * Format a string for use in a SQL query
     *
     * @param statement The SQL statement to format
     * @return The formatted SQL statement
     */
    @NotNull
    protected final String format(@NotNull String statement) {
        final Pattern pattern = Pattern.compile("%(\\w+)%");
        final Matcher matcher = pattern.matcher(statement);
        final StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            final Table table = Table.match(matcher.group(1));
            matcher.appendReplacement(sb, plugin.getSettings().getTableName(table));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Initialize the database connection
     *
     * @throws RuntimeException if the database initialization fails
     */
    public abstract void initialize() throws RuntimeException;

    /**
     * Get a user by their UUID
     *
     * @param uuid The UUID of the user
     * @return The user, if they exist
     */
    public abstract Optional<SavedUser> getUser(@NotNull UUID uuid);

    /**
     * Get a user by their name
     *
     * @param username The name of the user
     * @return The user, if they exist
     */
    public abstract Optional<SavedUser> getUser(@NotNull String username);

    /**
     * Add a user to the database
     *
     * @param user        The user to add
     * @param preferences The user's preferences
     */
    public abstract void createUser(@NotNull User user, @NotNull Preferences preferences);

    /**
     * Update a user's name and preferences in the database
     *
     * @param user        The user to update
     * @param preferences The user's preferences to update
     */
    public abstract void updateUser(@NotNull User user, @NotNull Preferences preferences);

    /**
     * Delete all users from the database
     */
    public abstract void deleteAllUsers();

    /**
     * Get a town by its id
     *
     * @param townId The id of the town
     * @return The town, if it exists
     */
    public abstract Optional<Town> getTown(int townId);

    /**
     * Get a list of all towns
     *
     * @return A list of all towns
     */
    public abstract List<Town> getAllTowns();

    /**
     * Add a town to the database
     *
     * @param name    The name of the town
     * @param creator The owner of the town
     * @return The created {@link Town}
     */
    @NotNull
    public abstract Town createTown(@NotNull String name, @NotNull User creator);

    /**
     * Update a town's name in the database
     *
     * @param town The town to update
     */
    public abstract void updateTown(@NotNull Town town);

    /**
     * Delete a town from the database
     *
     * @param townId The ID of the town to delete
     */
    public abstract void deleteTown(int townId);

    /**
     * Delete all towns from the database
     */
    public abstract void deleteAllTowns();

    /**
     * Get a list of all claim worlds on a server
     *
     * @return A list of all claim worlds on a server.
     * This will exclude {@link net.william278.husktowns.config.Settings#isUnclaimableWorld(World) unclaimable worlds}.
     */
    public abstract Map<World, ClaimWorld> getClaimWorlds(@NotNull String server);

    /**
     * Get a list of all claim worlds
     *
     * @return A map of world-server entries to each claim world
     */
    public abstract Map<ServerWorld, ClaimWorld> getAllClaimWorlds();

    /**
     * Create a new claim world and add it to the database
     *
     * @param world The world to create the claim world for
     * @return The created claim world
     */
    @NotNull
    public abstract ClaimWorld createClaimWorld(@NotNull World world);

    /**
     * Update a claim world in the database
     *
     * @param claimWorld The claim world to update
     */
    public abstract void updateClaimWorld(@NotNull ClaimWorld claimWorld);

    /**
     * Close the database connection
     */
    public abstract void close();

    /**
     * Check if the database has been loaded
     *
     * @return {@code true} if the database has loaded successfully; {@code false} if it failed to initialize
     */
    public boolean hasLoaded() {
        return loaded;
    }

    /**
     * Set if the database has loaded
     *
     * @param loaded whether the database has loaded successfully
     */
    protected void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * Identifies types of databases
     */
    public enum Type {
        MYSQL("MySQL"),
        SQLITE("SQLite");
        @NotNull
        private final String displayName;

        Type(@NotNull String displayName) {
            this.displayName = displayName;
        }

        @NotNull
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Represents the names of tables in the database
     */
    public enum Table {
        USER_DATA("husktowns_users"),
        TOWN_DATA("husktowns_town_data"),
        CLAIM_DATA("husktowns_claim_worlds");
        @NotNull
        private final String defaultName;

        Table(@NotNull String defaultName) {
            this.defaultName = defaultName;
        }

        @NotNull
        public static Database.Table match(@NotNull String placeholder) throws IllegalArgumentException {
            return Table.valueOf(placeholder.toUpperCase());
        }

        @NotNull
        public String getDefaultName() {
            return defaultName;
        }
    }
}
