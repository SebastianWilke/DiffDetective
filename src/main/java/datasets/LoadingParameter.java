package datasets;

/**
 * Read the input from a local directory, a local zip file or a remote repository.
 * 
 * @author Kevin Jedelhauser
 */
public enum LoadingParameter {
	
	/**
	 * Load repository from a local directory.
	 */
	FROM_DIR,
	
	/**
	 * Load repository from a local zip file.
	 */
	FROM_ZIP,
	
	/**
	 * Load repository from a remote location.
	 */
	FROM_REMOTE
}
