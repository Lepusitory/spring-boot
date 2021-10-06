package smoketest.session.hazelcast;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;


/**
 * Tests for {@link SampleSessionHazelcastApplication},
 * @author Susmitha Kandula
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SampleSessionHazelcastApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void test_sessionsEndPoint() {
		ResponseEntity<Map<String, Object>> entity = (ResponseEntity<Map<String, Object>>) (ResponseEntity) this.restTemplate.withBasicAuth("user", "password")
				.getForEntity("/actuator/sessions?username=user", Map.class);
		assertThat(entity).isNotNull();
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody().get("sessions")).isNotNull();
	}

}
