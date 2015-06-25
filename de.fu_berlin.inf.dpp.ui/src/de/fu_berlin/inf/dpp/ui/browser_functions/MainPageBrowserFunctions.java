package de.fu_berlin.inf.dpp.ui.browser_functions;

import com.google.gson.Gson;
import de.fu_berlin.inf.ag_se.browser.IBrowser;
import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.ui.core_facades.ContactListFacade;
import de.fu_berlin.inf.dpp.ui.ide_embedding.BrowserCreator;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.model.Account;
import de.fu_berlin.inf.dpp.ui.webpages.AddAccountPage;
import de.fu_berlin.inf.dpp.ui.webpages.BrowserPage;
import de.fu_berlin.inf.dpp.ui.webpages.SessionWizardPage;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * This class implements the functions to be called by Javascript code for
 * Saros main page. These are so-called browsers functions to invoke Java code from
 * Javascript.
 *
 * A note to future developers: the browser functions do not have to be split
 * according to webpages, it just suited the current state of the prototype.
 * Instead there may be more BrowserFunction classes per page and each BrowserFunction
 * class may be used be by many pages. Split them in such a way that no code duplication
 * arises.
 *
 * @JTourBusStop 1, Extending the HTML GUI, Calling Java from Javascript:
 *
 *               For calling Java from Javascript an instance of {@link JavascriptFunction}
 *               has to be created and injected into the browser
 *               by calling {@link IBrowser#createBrowserFunction(JavascriptFunction)}.
 *
 *               The classes in this package are used to bundle individual browser
 *               functions. The main criterion for the bundles is the avoidance
 *               of duplicate function declarations, the second is shared
 *               dependencies to Saros core classes, which are injected into this
 *               classes by PicoContainer.
 *
 *               The returned lists of browser functions are used by the {@link BrowserPage}
 *               implementations to collect all functions for its represented page.
 *               This is a many-to-many connection.
 *
 *               The actual injection of those functions is done by the {@link BrowserCreator}
 *
 *               The classes in the core_facades package can be used to bundle
 *               multiple core classes behind one facade.
 */
public class MainPageBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(MainPageBrowserFunctions.class);

    private final ContactListFacade contactListFacade;

    private final DialogManager dialogManager;

    private final AddAccountPage addAccountPage;

    private final SessionWizardPage sessionWizardPage;

    public MainPageBrowserFunctions(ContactListFacade contactListFacade,
        DialogManager dialogManager, AddAccountPage addAccountPage,
        SessionWizardPage sessionWizardPage) {
        this.contactListFacade = contactListFacade;
        this.dialogManager = dialogManager;
        this.addAccountPage = addAccountPage;
        this.sessionWizardPage = sessionWizardPage;
    }

    /**
     * Returns the list of browser functions encapsulated by this class.
     * They can be injected into a browser so that they can be called from Javascript.
     */
    public List<JavascriptFunction> getJavascriptFunctions() {
        return Arrays.asList(new JavascriptFunction("__java_connect") {
            @Override
            public Object function(Object[] arguments) {
                if (arguments.length > 0 && arguments[0] != null) {
                    Gson gson = new Gson();
                    final Account account = gson
                        .fromJson((String) arguments[0], Account.class);
                    ThreadUtils.runSafeAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            contactListFacade.connect(account);
                        }
                    });
                } else {
                    LOG.error("Connect was called without an account.");
                    browser.run(
                        "alert('Cannot connect because no account was given.');");
                }
                return null;
            }
        }, new JavascriptFunction("__java_disconnect") {
            @Override
            public Object function(Object[] arguments) {
                ThreadUtils.runSafeAsync(LOG, new Runnable() {
                    @Override
                    public void run() {
                        contactListFacade.disconnect();
                    }
                });
                return null;
            }
        }, new JavascriptFunction("__java_showAddAccountWizard") {
            @Override
            public Object function(Object[] arguments) {
                dialogManager.showDialogWindow(addAccountPage);
                return true;
            }
        }, new JavascriptFunction("__java_showStartSessionWizard") {
            @Override
            public Object function(Object[] arguments) {
                dialogManager.showDialogWindow(sessionWizardPage);
                return null;
            }
        });
    }
}
