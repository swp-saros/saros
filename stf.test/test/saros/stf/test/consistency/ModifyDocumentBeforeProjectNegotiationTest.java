package saros.stf.test.consistency;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;
import static saros.stf.shared.Constants.ACCEPT;
import static saros.stf.shared.Constants.SHELL_ADD_RESOURCES;
import static saros.stf.shared.Constants.SHELL_SESSION_INVITATION;

import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.test.stf.Constants;

public class ModifyDocumentBeforeProjectNegotiationTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  public void testModifyDocumentBeforeProjectNegotiation() throws Exception {

    Util.setUpSessionWithJavaProjectAndClass(
        Constants.PROJECT1, Constants.PKG1, Constants.CLS1, ALICE, BOB);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    ALICE.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    CARL.remoteBot().waitLongUntilShellIsOpen(SHELL_ADD_RESOURCES);

    // this test will fail if a jupiter proxy is added when bob is typing
    // text now
    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).typeText("The mighty Foobar");

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 10000);

    CARL.superBot().internal().createJavaProject(Constants.PROJECT1);

    CARL.superBot().confirmShellAddProjectUsingExistProject(Constants.PROJECT1);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).typeText(" bars everyfoo");

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(CARL.getJID(), 10000);

    CARL.remoteBot().editor(Constants.CLS1_SUFFIX).typeText("\n\n\nFoo yourself ");

    CARL.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 10000);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).navigateTo(0, 0);
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).typeText("blablablublub");

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 10000);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(CARL.getJID(), 10000);

    String aliceText = ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getText();
    String bobText = BOB.remoteBot().editor(Constants.CLS1_SUFFIX).getText();
    String carlText = CARL.remoteBot().editor(Constants.CLS1_SUFFIX).getText();

    assertEquals(aliceText, bobText);
    assertEquals(aliceText, carlText);
  }
}
