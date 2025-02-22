package ca.uhn.fhir.empi.svc;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.empi.model.CanonicalEID;
import ca.uhn.fhir.empi.rules.config.EmpiSettings;
import ca.uhn.fhir.empi.rules.json.EmpiRulesJson;
import ca.uhn.fhir.empi.util.EIDHelper;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;

import java.util.List;

import static ca.uhn.fhir.empi.api.EmpiConstants.HAPI_ENTERPRISE_IDENTIFIER_SYSTEM;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


public class EIDHelperR4Test {

	private static final FhirContext myFhirContext = FhirContext.forR4();
	private static final String EXTERNAL_ID_SYSTEM_FOR_TEST = "http://testsystem.io/naming-system/empi";

	private static final EmpiRulesJson myRules = new EmpiRulesJson() {{
		setEnterpriseEIDSystem(EXTERNAL_ID_SYSTEM_FOR_TEST);
	}};

	private static final EmpiSettings mySettings = new EmpiSettings() {{
		setEmpiRules(myRules);
	}};

	private static final EIDHelper EID_HELPER = new EIDHelper(myFhirContext, mySettings);


	@Test
	public void testExtractionOfInternalEID() {
		Patient patient = new Patient();
		patient.addIdentifier()
			.setSystem(HAPI_ENTERPRISE_IDENTIFIER_SYSTEM)
			.setValue("simpletest")
			.setUse(Identifier.IdentifierUse.SECONDARY);

		List<CanonicalEID> externalEid = EID_HELPER.getHapiEid(patient);

		assertThat(externalEid.isEmpty(), is(false));
		assertThat(externalEid.get(0).getValue(), is(equalTo("simpletest")));
		assertThat(externalEid.get(0).getSystem(), is(equalTo(HAPI_ENTERPRISE_IDENTIFIER_SYSTEM)));
		assertThat(externalEid.get(0).getUse(), is(equalTo("secondary")));
	}

	@Test
	public void testExtractionOfExternalEID() {
		String uniqueID = "uniqueID!";

		Patient patient = new Patient();
		patient.addIdentifier()
			.setSystem(EXTERNAL_ID_SYSTEM_FOR_TEST)
			.setValue(uniqueID);

		List<CanonicalEID> externalEid = EID_HELPER.getExternalEid(patient);

		assertThat(externalEid.isEmpty(), is(false));
		assertThat(externalEid.get(0).getValue(), is(equalTo(uniqueID)));
		assertThat(externalEid.get(0).getSystem(), is(equalTo(EXTERNAL_ID_SYSTEM_FOR_TEST)));
	}

	@Test
	public void testCreationOfInternalEIDGeneratesUuidEID() {

		CanonicalEID internalEid = EID_HELPER.createHapiEid();

		assertThat(internalEid.getSystem(), is(equalTo(HAPI_ENTERPRISE_IDENTIFIER_SYSTEM)));
		assertThat(internalEid.getValue().length(), is(equalTo(36)));
		assertThat(internalEid.getUse(), is(nullValue()));
	}
}
