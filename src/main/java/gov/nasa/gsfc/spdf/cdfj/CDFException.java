package gov.nasa.gsfc.spdf.cdfj;

/**
 * Base class for exceptions thrown by methods in this package.
 */
public class CDFException extends Exception {

    private static final long serialVersionUID = 5689815322568331571L;

    /**
     * Instantiates a new CDF exception.
     *
     * @param message the message
     */
    public CDFException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new CDF exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public CDFException(final String message, final Throwable cause) {
        super(message, cause);

    }

    /**
     * Instantiates a new CDF exception.
     *
     * @param cause the cause
     */
    public CDFException(final Throwable cause) {
        super(cause);
    }

    /**
     * Exceptions thrown by methods of CDFReader to indicate absence of
     * data for a variable.
     */
    public static class NoRecords extends CDFException {

        private static final long serialVersionUID = -2942078764040083006L;

        /**
         * Instantiates a new no records.
         *
         * @param variableName the var name
         */
        public NoRecords(final String variableName) {
            super("Variable " + variableName + " has no records.");
        }

        /**
         * Instantiates a new no records.
         *
         * @param message the message
         * @param cause   the cause
         */
        public NoRecords(final String message, final Throwable cause) {
            super(message, cause);
        }

        /**
         * Instantiates a new no records.
         *
         * @param cause the cause
         */
        public NoRecords(final Throwable cause) {
            super(cause);
        }
    }

    /**
     * Exceptions thrown by methods of CDFReader and its superclasses.
     */
    public static class ReaderError extends CDFException {

        private static final long serialVersionUID = 5741282201551022249L;

        /**
         * Instantiates a new reader error.
         *
         * @param message the message
         */
        public ReaderError(final String message) {
            super(message);
        }

        /**
         * Instantiates a new reader error.
         *
         * @param message the message
         * @param cause   the cause
         */
        public ReaderError(final String message, final Throwable cause) {
            super(message, cause);

        }

        /**
         * Instantiates a new reader error.
         *
         * @param cause the cause
         */
        public ReaderError(final Throwable cause) {
            super(cause);

        }
    }

    /**
     * Exceptions thrown by methods of CDFWriter and its superclass.
     */
    public static class WriterError extends CDFException {

        private static final long serialVersionUID = -2990962558147123833L;

        /**
         * Instantiates a new writer error.
         *
         * @param message the message
         */
        public WriterError(final String message) {
            super(message);
        }

        /**
         * Instantiates a new writer error.
         *
         * @param message the message
         * @param cause   the cause
         */
        public WriterError(final String message, final Throwable cause) {
            super(message, cause);

        }

        /**
         * Instantiates a new writer error.
         *
         * @param cause the cause
         */
        public WriterError(final Throwable cause) {
            super(cause);

        }
    }
}
