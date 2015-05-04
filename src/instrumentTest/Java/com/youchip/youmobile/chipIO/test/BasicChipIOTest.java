package instrumentTest.Java.com.youchip.youmobile.chipIO.test;

import android.test.InstrumentationTestCase;

import com.youchip.youmobile.model.chip.mc1kImpl.MC1KBasicChip;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs;

public class BasicChipIOTest extends InstrumentationTestCase {

    private static final byte[] uidValidBlock = new byte[]{(byte)0x01,(byte)0xAF,(byte)0xB7,(byte)0x00,(byte)0x10,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01};

    private static final byte[] invalidIDBlockNoRole         = new byte[]{(byte)0b0000_0000 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] validIDBlockAll              = new byte[]{(byte)0b0000_1111 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};

    private static final byte[] validIDBlockVisitor          = new byte[]{(byte)0b0000_0001 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] validIDBlockVisitorEmployee  = new byte[]{(byte)0b0000_0011 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] validIDBlockVisitorSupervisor= new byte[]{(byte)0b0000_0101 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] validIDBlockVisitorAdmin     = new byte[]{(byte)0b0000_1001 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};

    private static final byte[] validIDBlockEmployee         = new byte[]{(byte)0b0000_0010 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] validIDBlockSupervisor       = new byte[]{(byte)0b0000_0100 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] validIDBlockAdmin            = new byte[]{(byte)0b0000_1000 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};

    private static final byte[] validIDBlockEmployeeSupervisor          = new byte[]{(byte)0b0000_0110 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] validIDBlockEmployeeAdmin               = new byte[]{(byte)0b0000_1010 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};

    private static final byte[] validIDBlockVisitorEmployeeSupervisor   = new byte[]{(byte)0b0000_0111 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] validIDBlockVisitorEmployeeAdmin        = new byte[]{(byte)0b0000_1011 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] validIDBlockVisitorSupervisorAdmin      = new byte[]{(byte)0b0000_1101 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};

    private static final byte[] appTypeNone                 = new byte[]{(byte)0b0000_0000 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] appTypeConfig               = new byte[]{(byte)0b0001_0000 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] appTypeGateSwitch           = new byte[]{(byte)0b0010_0000 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] appTypeRest                 = new byte[]{(byte)0b0100_0000 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};

    private static final byte[] appTypeMixRole1             = new byte[]{(byte)0b0001_0001 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private static final byte[] appTypeMixRole2             = new byte[]{(byte)0b0001_0011 /*roles*/,(byte)0x03/*eventID*/,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};


    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testValidUID() throws Exception{
        MC1KBasicChip basicChip = new MC1KBasicChip();
        basicChip.setRawBlock(uidValidBlock,0);
        String uid = basicChip.getUID();
        assertEquals("01AFB700", uid);
    }

    public void testUIDFallback() throws Exception{
        MC1KBasicChip basicChip = new MC1KBasicChip();
        String uid = basicChip.getUID();
        assertEquals("00000000", uid);
    }

    public void testValidEventID() throws Exception {
        MC1KBasicChip basicChip = new MC1KBasicChip();
        basicChip.setRawBlock(invalidIDBlockNoRole,1);
        long eventID = basicChip.getEventID();
        assertEquals(3,eventID);
    }

    public void testHasVisitorRole() throws Exception {
        MC1KBasicChip basicChip = new MC1KBasicChip();

        basicChip.setRawBlock(validIDBlockAll,1);
        assertTrue(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockVisitor,1);
        assertTrue(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockVisitorEmployee,1);
        assertTrue(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockVisitorSupervisor,1);
        assertTrue(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockVisitorAdmin,1);
        assertTrue(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockVisitorEmployeeSupervisor,1);
        assertTrue(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockVisitorEmployeeAdmin,1);
        assertTrue(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockVisitorSupervisorAdmin,1);
        assertTrue(basicChip.isVisitor());
    }

    /**
     * Tests the role on chip and if the chip does NOT have a visitor role if the first bit
     * is zero
     * @throws Exception
     */
    public void testHasNoVisitorRole() throws Exception {
        MC1KBasicChip basicChip = new MC1KBasicChip();

        basicChip.setRawBlock(invalidIDBlockNoRole,1);
        assertFalse(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockEmployee,1);
        assertFalse(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockSupervisor,1);
        assertFalse(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockAdmin,1);
        assertFalse(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockEmployeeSupervisor,1);
        assertFalse(basicChip.isVisitor());

        basicChip.setRawBlock(validIDBlockEmployeeAdmin,1);
        assertFalse(basicChip.isVisitor());
    }

    public void testHasEmployeeRole() throws Exception {
        MC1KBasicChip basicChip = new MC1KBasicChip();

        basicChip.setRawBlock(validIDBlockAll,1);
        assertTrue(basicChip.isEmployee());

        basicChip.setRawBlock(validIDBlockVisitorEmployee,1);
        assertTrue(basicChip.isEmployee());

        basicChip.setRawBlock(validIDBlockEmployee,1);
        assertTrue(basicChip.isEmployee());

        basicChip.setRawBlock(validIDBlockEmployeeSupervisor,1);
        assertTrue(basicChip.isEmployee());

        basicChip.setRawBlock(validIDBlockVisitorEmployeeSupervisor,1);
        assertTrue(basicChip.isEmployee());

        basicChip.setRawBlock(validIDBlockVisitorEmployeeAdmin,1);
        assertTrue(basicChip.isEmployee());

        // inherited roles
        basicChip.setRawBlock(validIDBlockVisitorSupervisor,1);
        assertTrue(basicChip.isEmployee());

        basicChip.setRawBlock(validIDBlockVisitorAdmin,1);
        assertTrue(basicChip.isEmployee());

        basicChip.setRawBlock(validIDBlockSupervisor,1);
        assertTrue(basicChip.isEmployee());

        basicChip.setRawBlock(validIDBlockAdmin,1);
        assertTrue(basicChip.isEmployee());

        basicChip.setRawBlock(validIDBlockVisitorSupervisorAdmin,1);
        assertTrue(basicChip.isEmployee());
    }

    public void testHasNoEmployeeRole() throws Exception {
        MC1KBasicChip basicChip = new MC1KBasicChip();

        basicChip.setRawBlock(invalidIDBlockNoRole,1);
        assertFalse(basicChip.isEmployee());

        basicChip.setRawBlock(validIDBlockVisitor,1);
        assertFalse(basicChip.isEmployee());
    }

    public void testAppTypeReadConsistency() throws Exception {
        MC1KBasicChip basicChip = new MC1KBasicChip();

        basicChip.setRawBlock(appTypeNone,1);
        assertTrue(basicChip.getAppType().equals(MC1KChipSpecs.AppType.NO_APP));

        basicChip.setRawBlock(appTypeGateSwitch,1);
        assertTrue(basicChip.getAppType().equals(MC1KChipSpecs.AppType.GATE_SWITCHER_APP));

        basicChip.setRawBlock(appTypeRest,1);
        assertTrue(basicChip.getAppType().equals(MC1KChipSpecs.AppType.RESET_APP));

        basicChip.setRawBlock(appTypeConfig, 1);
        assertTrue(basicChip.getAppType().equals(MC1KChipSpecs.AppType.CONFIG_LOAD_APP));
    }

    public void testAppTypeIsNoVisitor() throws Exception {
        MC1KBasicChip basicChip = new MC1KBasicChip();

        basicChip.setRawBlock(appTypeNone,1);
        assertFalse(basicChip.getAppType().equals(MC1KChipSpecs.AppType.VISITOR_APP));

        basicChip.setRawBlock(appTypeGateSwitch,1);
        assertFalse(basicChip.getAppType().equals(MC1KChipSpecs.AppType.VISITOR_APP));

        basicChip.setRawBlock(appTypeRest,1);
        assertFalse(basicChip.getAppType().equals(MC1KChipSpecs.AppType.VISITOR_APP));

        basicChip.setRawBlock(appTypeConfig, 1);
        assertFalse(basicChip.getAppType().equals(MC1KChipSpecs.AppType.VISITOR_APP));
    }

    public void testAppTypeIsVisitor() throws Exception {
        MC1KBasicChip basicChip = new MC1KBasicChip();

        basicChip.setRawBlock(appTypeMixRole1,1);
        assertTrue(basicChip.getAppType().equals(MC1KChipSpecs.AppType.VISITOR_APP));

        basicChip.setRawBlock(appTypeMixRole2,1);
        assertTrue(basicChip.getAppType().equals(MC1KChipSpecs.AppType.VISITOR_APP));

        basicChip.setRawBlock(appTypeMixRole1,1);
        assertFalse(basicChip.getAppType().equals(MC1KChipSpecs.AppType.CONFIG_LOAD_APP));

        basicChip.setRawBlock(appTypeMixRole2,1);
        assertFalse(basicChip.getAppType().equals(MC1KChipSpecs.AppType.CONFIG_LOAD_APP));

    }
}