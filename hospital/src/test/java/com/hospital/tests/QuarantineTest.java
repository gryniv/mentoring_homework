package com.hospital.tests;

import com.hospital.Quarantine;
import com.hospital.exception.UnknownHealthConditionException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * The responsibility of the Quarantine object is to simulate diseases on a group of patients.
 * It is initialized with a list of patients' health status, separated by a comma.
 * Each health status is described by one or more characters
 * (in the test below, we will always have only one disease / patient)
 * <p>
 * The characters mean:
 * H : Healthy
 * F : Fever
 * D : Diabetes
 * T : Tuberculosis
 * <p>
 * Quarantine provides medicines to the patients, but can not target a specific group of patient.
 * The same medicines are always given to all the patients.
 * <p>
 * Then Quarantine can provide a report with this format:
 * "F:1 H:2 D:0 T:1 X:3"
 * <p>
 * Report give the number of patients that have the given disease.
 * X means Dead
 */

public class QuarantineTest {

    @Test
    public void withoutTreatmentNorTime() throws UnknownHealthConditionException {
        Quarantine quarantine = new Quarantine("F,H,D,T");
        assertEquals("F:1 H:1 D:1 T:1 X:0", quarantine.report());
    }

    @Test
    public void withoutTreatment() throws UnknownHealthConditionException {
        Quarantine quarantine = new Quarantine("F,H,D,D,D,T");
        quarantine.wait40Days();
        // diabetics die without insulin
        assertEquals("F:1 H:1 D:0 T:1 X:3", quarantine.report());
    }

    @Test
    public void aspirin() throws UnknownHealthConditionException {
        Quarantine quarantine = new Quarantine("F,F,F,H,D,T");
        quarantine.aspirin();
        quarantine.wait40Days();
        // aspirin cure Fever
        assertEquals("F:0 H:4 D:0 T:1 X:1", quarantine.report());
    }

    @Test
    public void antibiotic() throws UnknownHealthConditionException {
        Quarantine quarantine = new Quarantine("F,H,D,D,D,H,T");
        quarantine.antibiotic();
        quarantine.wait40Days();
        // antibiotic cure Tuberculosis
        assertEquals("F:1 H:3 D:0 T:0 X:3", quarantine.report());
    }

    @Test
    public void insulin() throws UnknownHealthConditionException {
        Quarantine quarantine = new Quarantine("F,H,D,D,D,H,T");
        quarantine.insulin();
        quarantine.wait40Days();
        // insulin prevent diabetic subject from dying, does not cure Diabetes,
        assertEquals("F:1 H:2 D:3 T:1 X:0", quarantine.report());
    }

    @Test
    public void antibioticPlusInsulin() throws UnknownHealthConditionException {
        Quarantine quarantine = new Quarantine("F,H,D,D,D,H,T");
        quarantine.antibiotic();
        quarantine.insulin();
        quarantine.wait40Days();
        // if insulin is mixed with antibiotic, healthy people catch Fever
        assertEquals("F:3 H:1 D:3 T:0 X:0", quarantine.report());
    }

    @Test
    public void paracetamol() throws UnknownHealthConditionException {
        Quarantine quarantine = new Quarantine("F,F,H,D,D,D,H,T");
        quarantine.paracetamol();
        quarantine.wait40Days();
        // paracetamol heals fever
        assertEquals("F:0 H:4 D:0 T:1 X:3", quarantine.report());
    }

    @Test
    public void paracetamolAndAspirin() throws UnknownHealthConditionException {
        Quarantine quarantine = new Quarantine("F,H,D,D,D,H,T");
        quarantine.paracetamol();
        quarantine.aspirin();
        // mixing paracetamol & aspirin INSTANTLY kills subject !
        assertEquals("F:0 H:0 D:0 T:0 X:7", quarantine.report());
    }
}
