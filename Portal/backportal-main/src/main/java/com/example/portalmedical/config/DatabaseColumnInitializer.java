package com.example.portalmedical.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseColumnInitializer implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseColumnInitializer.class);

	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;

	public DatabaseColumnInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
		this.dataSource = dataSource;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		ensureLongTextColumn(Arrays.asList("FichePatient", "fichepatient", "fiche_patient"),
				Arrays.asList("Causedeconsultation", "Examen", "Suivie"));
		ensureLongTextColumn(Arrays.asList("Certificats", "certificats"),
				Arrays.asList("commantaire"));
		ensureLongTextColumn(Arrays.asList("certificatsaptitude", "CertificatsAptitude", "certificats_aptitude"),
				Arrays.asList("commantaire"));
	}

	private void ensureLongTextColumn(List<String> tableCandidates, List<String> columns) {
		String tableName = resolveExistingTable(tableCandidates);
		if (tableName == null) {
			logger.warn("Skipping column migration because no matching table was found for {}", tableCandidates);
			return;
		}

		for (String column : columns) {
			try {
				jdbcTemplate.execute(
						"ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + column + "` LONGTEXT");
			} catch (Exception ex) {
				logger.warn("Could not widen {}.{} to LONGTEXT: {}", tableName, column, ex.getMessage());
			}
		}
	}

	private String resolveExistingTable(List<String> tableCandidates) {
		Set<String> normalizedCandidates = new HashSet<>();
		for (String candidate : tableCandidates) {
			normalizedCandidates.add(normalize(candidate));
		}

		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			try (ResultSet tables = metaData.getTables(connection.getCatalog(), null, "%", new String[] { "TABLE" })) {
				while (tables.next()) {
					String tableName = tables.getString("TABLE_NAME");
					if (normalizedCandidates.contains(normalize(tableName))) {
						return tableName;
					}
				}
			}
		} catch (SQLException ex) {
			logger.warn("Unable to inspect database metadata: {}", ex.getMessage());
		}
		return null;
	}

	private String normalize(String value) {
		return value == null ? "" : value.replace("_", "").toLowerCase(Locale.ROOT);
	}
}
