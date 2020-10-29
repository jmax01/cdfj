package gov.nasa.gsfc.spdf.cdfj;

/**
 * Base class for exceptions thrown by methods in this package.
 */
public class CDFException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 5689815322568331571L;

    /**
     *
     * @param string
     */
    public CDFException(String message) {
        super(message);
    }

    /**
     * Exceptions thrown by methods of CDFReader to indicate absence of
     * data for a variable.
     */
    public static class NoRecords extends CDFException {

        /**
         *
         */
        private static final long serialVersionUID = -2942078764040083006L;

        /**
         *
         * @param varName
         */
        public NoRecords(String varName) {
            super("Variable " + varName + " has no records.");
        }
    }

    /**
     * Exceptions thrown by methods of CDFReader and its superclasses.
     */
    public static class ReaderError extends CDFException {

        /**
         *
         */
        private static final long serialVersionUID = 5741282201551022249L;

        /**
         *
         * @param string
         */
        public ReaderError(String message) {
            super(message);
        }
    }

    /**
     * Exceptions thrown by methods of CDFWriter and its superclass.
     */
    public static class WriterError extends CDFException {

        /**
         *
         */
        private static final long serialVersionUID = -2990962558147123833L;

        /**
         *
         * @param string
         */
        public WriterError(String message) {
            super(message);
        }
    }
}
