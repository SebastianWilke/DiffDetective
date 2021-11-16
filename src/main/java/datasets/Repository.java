package datasets;

import java.net.URI;
import java.net.URISyntaxException;

import org.pmw.tinylog.Logger;

/**
 * Repository containing data.
 * 
 * @author Kevin Jedelhauser
 */
public class Repository {
	
	/**
	 * The location from where the input repository is read from.
	 */
	private LoadingParameter repoLocation;
	
	/**
	 * The local or remote URI of a repository.
	 */
	private final String repositoryURI;

	/**
	 * The name of the cloned folder for remote repository only. May be <code>null</code> if local. 
	 */
	private final String repositoryName;
	
	/**
	 * For large repositories.
	 * Alternatively, increasing the java heap size also helps.
	 */
	private final boolean saveMemory;
	
	/**
	 * Creates a repository.
	 * 
	 * @param repoLocation {@link LoadingParameter} From which location the repository is read from
	 * @param repositoryPath The local or remote path of the repository
	 * @param repositoryName Name of the cloned repository (<code>null</code> if local)
	 * @param saveMemory
	 */
	public Repository(final LoadingParameter repoLocation,
			final URI repositoryURI,
			final String repositoryName,
			final boolean saveMemory) {
		this.repoLocation = repoLocation;
		this.repositoryURI = repositoryURI.toString();
		this.repositoryName = repositoryName;
		this.saveMemory = saveMemory;
	}

	/**
	 * Creates a repository from an existing directory.
	 * 
	 * @param dirURI The path URL to the repo directory relative to <PROJECT_FOLDER>/repositories
	 * @param repoName A name for the repository (currently not used)
	 * @return A repository from an existing directory
	 */
	public static Repository fromDirectory(URI dirURI, String repoName) {
		return new Repository(LoadingParameter.FROM_DIR,
				dirURI,
				repoName,
				true);
	}
	
	/**
	 * Creates a repository from a local zip file.
	 * 
	 * @param fileURI The path URL to the zip file relative to <PROJECT_FOLDER>/repositories
	 * @param repoName A name for the repository (currently not used)
	 * @return A repository from a local zip file
	 */
	public static Repository fromZip(URI fileURI, String repoName) {
		return new Repository(LoadingParameter.FROM_ZIP,
				fileURI,
				repoName,
				true);
	}

	/**
	 * Creates a repository from a remote repository.
	 * 
	 * @param repoUri The address of the remote repository
	 * @param repoName Name of the folder, where the git repository is cloned to
	 * @return A repository from a remote location (e.g. Github repository)
	 */
	public static Repository fromRemote(URI repoUri, String repoName) {
		return new Repository(LoadingParameter.FROM_REMOTE,
				repoUri,
				repoName,
				true);
	}
	
	/**
	 * Creates a predefined Marlin repository.
	 * 
	 * @return Marlin repository
	 */		
	public static Repository createMarlinZipRepo() {
		Repository repo = null;
		try {
			URI marlinURI = new URI("Marlin_old.zip");
			repo = fromZip(marlinURI, "Marlin_old");
		} catch (URISyntaxException e) {
			Logger.error(e);
		}
		return repo;
	}

	/**
	 * Creates a predefined Linux repository from a remote location.
	 * 
	 * @return Linux repository
	 */
	public static Repository createRemoteLinuxRepo() {
		Repository repo = null;
		try {
			URI linuxURI = new URI("https://github.com/torvalds/linux");
			repo = fromRemote(linuxURI, "linux_remote");
		} catch (URISyntaxException e) {
			Logger.error(e);
		}
		return repo;
	}

	/**
	 * Creates a predefined Busybox repository from a remote location.
	 * 
	 * @return Busybox repository
	 */
	public static Repository createRemoteBusyboxRepo() {
		Repository repo = null;
		try {
			URI busyboxURI = new URI("https://git.busybox.net/busybox");
			repo = fromRemote(busyboxURI, "busybox_remote");
		} catch (URISyntaxException e) {
			Logger.error(e);
		}
		return repo;
	}

	/**
	 * Creates a predefined Vim repository from a remote location.
	 * 
	 * @return Vim repository
	 */
	public static Repository createRemoteVimRepo() {
		Repository repo = null;
		try {
			URI vimURI = new URI("https://github.com/vim/vim");
			repo = fromRemote(vimURI, "vim_remote");
		} catch (URISyntaxException e) {
			Logger.error(e);
		}
		return repo;
	}

	/**
	 * Creates a predefined libssh repository from a remote location.
	 * 
	 * @return libssh repository
	 */
	public static Repository createRemoteLibsshRepo() {
		Repository repo = null;
		try {
			URI libsshURI = new URI("https://gitlab.com/libssh/libssh-mirror");
			repo = fromRemote(libsshURI, "libssh_remote");
		} catch (URISyntaxException e) {
			Logger.error(e);
		}
		return repo;
	}

	public LoadingParameter getRepoLocation() {
		return repoLocation;
	}
	
	public String getRepositoryURI() {
		return repositoryURI;
	}
	
	public String getRepositoryName() {
		return repositoryName;
	}

	public boolean shouldSaveMemory() {
		return saveMemory;
	}
}