import java.io.File;
import java.util.Date;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import play.Logger;

public class CleanupActor extends UntypedActor {

	private int fileExpirationDuration = 60000;

	/**
	 * Create Props for an actor of this type.
	 * 
	 * @param magicNumber
	 *            The magic number to be passed to this actor’s constructor.
	 * @return a Props for creating this actor, which can then be further
	 *         configured (e.g. calling `.withDispatcher()` on it)
	 */
	public static Props props(final int magicNumber) {
		return Props.create(new Creator<CleanupActor>() {
			private static final long serialVersionUID = 1L;

			@Override
			public CleanupActor create() throws Exception {
				return new CleanupActor(magicNumber);
			}
		});
	}

	final int magicNumber;

	public CleanupActor(int magicNumber) {
		this.magicNumber = magicNumber;
	}

	@Override
	public void onReceive(Object msg) {
		//Logger.debug("Executing actor: " + msg.toString());
		cleanFiles();
	}

	public void cleanFiles() {
		String[] resources = { "pdf", "docx" };
		for (String resource : resources) {
			File path = new File("resources/"+resource+"/");
			for (File file : path.listFiles()) {
				if (file.isFile() && file.getAbsoluteFile().toString().endsWith("."+resource)) {
					long diff = new Date().getTime() - file.lastModified();
					if (diff > fileExpirationDuration) {
						Logger.debug("Deleting file: " + file.getAbsolutePath() + " while created before : "
								+ diff / 1000 + "s");
						file.delete();
					}
				}
			}
		}
	}

}