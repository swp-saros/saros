package saros.intellij.eventhandler;

import com.intellij.openapi.project.Project;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import saros.intellij.editor.EditorManager;
import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.filesystem.LocalFilesystemModificationHandler;
import saros.observables.FileReplacementInProgressObservable;
import saros.session.ISarosSession;

/** Factory to instantiate {@link ApplicationEventHandlers} objects. */
public class ApplicationEventHandlersFactory {

  private final EditorManager editorManager;
  private final ISarosSession sarosSession;
  private final FileReplacementInProgressObservable fileReplacementInProgressObservable;
  private final AnnotationManager annotationManager;
  private final LocalEditorHandler localEditorHandler;

  public ApplicationEventHandlersFactory(
      EditorManager editorManager,
      ISarosSession sarosSession,
      FileReplacementInProgressObservable fileReplacementInProgressObservable,
      AnnotationManager annotationManager,
      LocalEditorHandler localEditorHandler) {

    this.editorManager = editorManager;
    this.sarosSession = sarosSession;
    this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
    this.annotationManager = annotationManager;
    this.localEditorHandler = localEditorHandler;
  }

  /**
   * Returns a new <code>ApplicationEventHandlers</code> instance with handlers for the currently
   * running application/Intellij instance.
   *
   * <p><b>NOTE:</b> Consider carefully whether you want to instantiate and start a new set of
   * application-level listeners as this should generally only be done once in in the beginning of
   * the session.
   *
   * <p><b>NOTE:</b>Currently, some of the application event handlers still rely on a project
   * instance. This has to be adjusted before multiple projects are shareable, at which point the
   * project parameter will be removed from this method.
   *
   * @param project the project to instantiate handlers for
   * @return a new <code>ApplicationEventHandlers</code> instance with handlers for the currently
   *     running application/Intellij instance
   */
  public ApplicationEventHandlers createApplicationEventHandler(@NotNull Project project) {
    Set<IApplicationEventHandler> applicationEventHandlers = new HashSet<>();

    /*
     * local filesystem modification handlers
     */
    /*
     * TODO make this a real application component
     *  The handler listens to the whole virtual filesystem but still uses the project.
     *  This has to be adjusted before sharing multiple reference points/projects is enabled.
     */
    applicationEventHandlers.add(
        new LocalFilesystemModificationHandler(
            project,
            editorManager,
            sarosSession,
            fileReplacementInProgressObservable,
            annotationManager,
            localEditorHandler));

    return new ApplicationEventHandlers(applicationEventHandlers);
  }
}
