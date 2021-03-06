package saros.server.filesystem;

import java.io.IOException;
import java.nio.file.Path;
import saros.exceptions.OperationCanceledException;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.filesystem.IWorkspaceRunnable;
import saros.monitoring.NullProgressMonitor;

/** Server implementation of the {@link IWorkspace} interface. */
public class ServerWorkspaceImpl implements IWorkspace {

  private Path location;

  /**
   * Creates a ServerWorkspaceImpl.
   *
   * @param location the workspace's absolute root location in the file system
   */
  public ServerWorkspaceImpl(Path location) {
    this.location = location;
  }

  public Path getLocation() {
    return location;
  }

  public IReferencePoint getProject(String name) {
    return new ServerProjectImpl(this, name);
  }

  @Override
  public void run(IWorkspaceRunnable runnable) throws IOException, OperationCanceledException {

    run(runnable, null);
  }

  @Override
  public void run(IWorkspaceRunnable runnable, IResource[] resources)
      throws IOException, OperationCanceledException {

    /*
     * TODO Implement a watchdog to interrupt runnables that run too long
     */
    synchronized (this) {
      runnable.run(new NullProgressMonitor());
    }
  }
}
