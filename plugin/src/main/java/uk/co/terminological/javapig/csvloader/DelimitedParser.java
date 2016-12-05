package uk.co.terminological.javapig.csvloader;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.javapig.util.SourceCode;

/**
 * A utility for reading delimited files such as CSV, TSV or pipe delimited files.
 * This can handle different separators, line endings, and mandatory or optional enclosure
 * of the fields, with escaping within enclosed fields. Usual configurations are provided by the static factory 
 * methods or you can roll your own with the constructors. This can handle multi character delimiters and terminators
 * @author terminological
 *
 */
public class DelimitedParser {

	Tokeniser lex;
	StateMachine machine;
	Reader reader;
	static Logger log = LoggerFactory.getLogger(DelimitedParser.class);

	public static DelimitedParser windowsCsv(Reader in) {
		return new DelimitedParser(in,",","\r\n","\"","\"",false);
	}

	public static DelimitedParser csv(Reader in) {
		return new DelimitedParser(in,",","\n","\"","\"",false);
	}

	public static DelimitedParser windowsTsv(Reader in) {
		return new DelimitedParser(in,"\t","\r\n");
	}
	
	public static DelimitedParser tsv(Reader in) {
		return new DelimitedParser(in,"\t","\n");
	}

	public static DelimitedParser windowsPipe(Reader in) {
		return new DelimitedParser(in,"|","\n");
	}
	
	public static DelimitedParser pipe(Reader in) {
		return new DelimitedParser(in,"|","\n");
	}

	/**
	 * Creates a delimited file parser with custom options which fields are not enclosed or escaped, and delimiters are disallowed within fields.
	 * This is a common configuration for e.g. tsv or pipe seperated
	 * @param reader - a reader providing input
	 * @param sep - the separator - eg. ","
	 * @param term - the terminator - eg. "\r\n"
	 */
	public DelimitedParser(Reader reader, String sep, String term) {
		this.reader = reader;
		this.lex = new Tokeniser(sep,term);
		// No enclosures
		this.machine = StateMachine
				.inState(State.LINE_TERMINATED)

				.withTransition(State.FIELD_TERMINATED, t->t.matched(sep), State.FIELD_TERMINATED)
				.withTransition(State.FIELD_TERMINATED, t->t.matched(term), State.LINE_TERMINATED)
				.withTransition(State.FIELD_TERMINATED, t->t instanceof EOF, State.LINE_TERMINATED)
				.withTransition(State.FIELD_TERMINATED, t->(!(t instanceof EOF) && !t.matched(sep) && !t.matched(term)), State.READING_UNENCLOSED)

				.withTransition(State.READING_UNENCLOSED, t->t.matched(sep), State.FIELD_TERMINATED)
				.withTransition(State.READING_UNENCLOSED, t->t.matched(term), State.LINE_TERMINATED)
				.withTransition(State.READING_UNENCLOSED, t->(!t.matched(sep) && !t.matched(term)), State.READING_UNENCLOSED)

				.withTransition(State.LINE_TERMINATED, t->t.matched(sep), State.FIELD_TERMINATED)
				.withTransition(State.LINE_TERMINATED, t->t.matched(term), State.LINE_TERMINATED)
				.withTransition(State.LINE_TERMINATED, t->t instanceof EOF, State.FILE_TERMINATED)
				.withTransition(State.LINE_TERMINATED, t->(!(t instanceof EOF) && !t.matched(sep) && !t.matched(term)), State.READING_UNENCLOSED)
				;
	}

	/**
	 * Creates a delimited file parser with custom options which has the same escape character as enclosure and enclosure is mandatory.
	 * This is a common configuration for e.g. csv from MS Excel.
	 * @param reader - a reader providing input
	 * @param sep - the separator - eg. ","
	 * @param term - the terminator - eg. "\r\n"
	 * @param enc - the enclosing characters - eg. "\""
	 */
	
	public DelimitedParser(Reader reader, String sep, String term, String enc) {
		this(reader,sep,term,enc,enc, true);
	}

	/**
	 * Creates a delimited file parser with custom options
	 * @param reader - a reader providing input
	 * @param sep - the separator - eg. ","
	 * @param term - the terminator - eg. "\r\n"
	 * @param enc - the enclosing characters - eg. "\""
	 * @param escape - the escaping characters - e.g. "\\"
	 * @param enclosedMandatory - defines whether fields are always enclosed or not
	 */
	public DelimitedParser(Reader reader, String sep, String term, String enc, String escape, boolean enclosedMandatory) {
		this.reader = reader;
		this.lex = new Tokeniser(enc,sep,term,escape);
		if (!enclosedMandatory) {
			// Optional enclosure
			this.machine = StateMachine
					.inState(State.LINE_TERMINATED)

					.withTransition(State.FIELD_TERMINATED, t->t.matched(enc), State.ENCLOSING_FIELD)
					.withTransition(State.FIELD_TERMINATED, t->t.matched(sep), State.FIELD_TERMINATED)
					.withTransition(State.FIELD_TERMINATED, t->t.matched(term), State.LINE_TERMINATED)
					.withTransition(State.FIELD_TERMINATED, t->t instanceof EOF, State.LINE_TERMINATED)
					.withTransition(State.FIELD_TERMINATED, t->(!(t instanceof EOF) && !t.matched(enc) && !t.matched(sep) && !t.matched(term) && !t.matched(escape)), State.READING_UNENCLOSED)

					.withTransition(State.READING_UNENCLOSED, t->t.matched(sep), State.FIELD_TERMINATED)
					.withTransition(State.READING_UNENCLOSED, t->t.matched(term), State.LINE_TERMINATED)
					.withTransition(State.READING_UNENCLOSED, t->(!t.matched(sep) && !t.matched(term) && !t.matched(escape)), State.READING_UNENCLOSED)

					.withTransition(State.ENCLOSING_FIELD, t->t.matched(escape), State.ESCAPING_ENCLOSED)
					.withTransition(State.ENCLOSING_FIELD, t->!t.matched(escape), State.READING_ENCLOSED)

					.withTransition(State.READING_ENCLOSED, t->t.matched(escape), State.ESCAPING_ENCLOSED)
					.withTransition(State.READING_ENCLOSED, t->t.matched(enc), State.UNENCLOSING_FIELD)
					.withTransition(State.READING_ENCLOSED, t->(!t.matched(enc) && !t.matched(escape)), State.READING_ENCLOSED)

					.withTransition(State.ESCAPING_ENCLOSED, t->true, State.READING_ENCLOSED)

					.withTransition(State.UNENCLOSING_FIELD, t->t.matched(term), State.LINE_TERMINATED)
					.withTransition(State.UNENCLOSING_FIELD, t->t.matched(sep), State.FIELD_TERMINATED)

					.withTransition(State.LINE_TERMINATED, t->t.matched(enc), State.ENCLOSING_FIELD)
					.withTransition(State.LINE_TERMINATED, t->t.matched(sep), State.FIELD_TERMINATED)
					.withTransition(State.LINE_TERMINATED, t->t.matched(term), State.LINE_TERMINATED)
					.withTransition(State.LINE_TERMINATED, t->t instanceof EOF, State.FILE_TERMINATED)
					.withTransition(State.LINE_TERMINATED, t->(!(t instanceof EOF) && !t.matched(enc) && !t.matched(sep) && !t.matched(term) && !t.matched(escape)), State.READING_UNENCLOSED);
		} else {
			//Mandatory enclosure
			this.machine = StateMachine
					.inState(State.LINE_TERMINATED)

					.withTransition(State.FIELD_TERMINATED, t->t.matched(enc), State.ENCLOSING_FIELD)
					.withTransition(State.FIELD_TERMINATED, t->t.matched(sep), State.FIELD_TERMINATED)
					.withTransition(State.FIELD_TERMINATED, t->t.matched(term), State.LINE_TERMINATED)
					.withTransition(State.FIELD_TERMINATED, t->t instanceof EOF, State.LINE_TERMINATED)

					.withTransition(State.ENCLOSING_FIELD, t->t.matched(escape), State.ESCAPING_ENCLOSED)
					.withTransition(State.ENCLOSING_FIELD, t->!t.matched(escape), State.READING_ENCLOSED)

					.withTransition(State.READING_ENCLOSED, t->t.matched(escape), State.ESCAPING_ENCLOSED)
					.withTransition(State.READING_ENCLOSED, t->t.matched(enc), State.UNENCLOSING_FIELD)
					.withTransition(State.READING_ENCLOSED, t->(!t.matched(enc) && !t.matched(escape)), State.READING_ENCLOSED)

					.withTransition(State.ESCAPING_ENCLOSED, t->true, State.READING_ENCLOSED)

					.withTransition(State.UNENCLOSING_FIELD, t->t.matched(term), State.LINE_TERMINATED)
					.withTransition(State.UNENCLOSING_FIELD, t->t.matched(sep), State.FIELD_TERMINATED)

					.withTransition(State.LINE_TERMINATED, t->t.matched(enc), State.ENCLOSING_FIELD)
					.withTransition(State.LINE_TERMINATED, t->t.matched(sep), State.FIELD_TERMINATED)
					.withTransition(State.LINE_TERMINATED, t->t.matched(term), State.LINE_TERMINATED)
					.withTransition(State.LINE_TERMINATED, t->t instanceof EOF, State.FILE_TERMINATED);

		}
	}

	public DelimitedParser(Reader reader, String sep, String term, String enc, String escape) {
		this(reader,sep,term,enc,escape,false);
	}

	/**
	 * The readLine method parses the next line from the delimited stream.
	 * @return A list of strings representing the fields in a single line of the file
	 * @throws EOFException
	 * @throws MalformedCSVException
	 */
	public List<String> readLine() throws EOFException, MalformedCSVException {
		Iterator<Token> tokenProvider = lex.tokenise(reader);
		StringBuilder out = new StringBuilder();
		ArrayList<String> out2 = new ArrayList<>();
		while(true) {
			Token token = tokenProvider.next();
			State start = machine.current;
			State result = machine.execute(token);
			log.debug("token: "+token+": transition: "+start+"->"+result);
			switch (result) {
			case READING_ENCLOSED:
			case READING_UNENCLOSED:
				out.append(token.get());
				break;
			case FIELD_TERMINATED:
				log.debug("new field: "+out.toString());
				out2.add(out.toString());
				out = new StringBuilder();
				break;
			case LINE_TERMINATED:
				out2.add(out.toString());
				log.debug("new field: "+out.toString());
				log.debug("completed line");
				return out2;
			case FILE_TERMINATED:
				throw new EOFException();
			default:
				break;
			};
		}
	}
	
	public Iterable<List<String>> readLines() {
		return new Iterable<List<String>>() {
			@Override
			public Iterator<List<String>> iterator() {
				return new Iterator<List<String>>() {
					List<String> cache;
					boolean ready = false;
					public boolean hasNext() {
						if (!ready) {
							try {
								cache = readLine();
								ready = true;
							} catch (EOFException e) {
								return false;
							} catch (MalformedCSVException e) {
								throw new RuntimeException("Malformed CSV",e);
							}
						}
						return ready;
					}
					@Override
					public List<String> next() {
						if (hasNext()) {
							ready = false;
							return cache;
						} else {
							throw new NoSuchElementException();
						}
					}
				};
			}
		};
	}

	public Stream<List<String>> stream() {
		return StreamSupport.stream(readLines().spliterator(),false);
	}
	
	
	private static class StateMachine {
		State current;
		Map<State,List<Transition>> transitions = new EnumMap<State,List<Transition>>(State.class);
		public static StateMachine inState(State state) {
			StateMachine out = new StateMachine();
			out.current = state;
			return out;
		}
		public StateMachine withTransition(State in, Predicate<Token> message, State end) {
			if (transitions.get(in) == null) transitions.put(in, new ArrayList<>());
			transitions.get(in).add(Transition.from(message, end));
			return this;
		}
		public State execute(Token token) throws MalformedCSVException {
			current = transitions.get(current).stream()
					.filter(t -> t.message.test(token))
					.map(t -> t.end)
					.findFirst().orElseThrow(() -> new MalformedCSVException(current,token));
			return current;
		}
	}


	private enum State {
		READING_ENCLOSED,
		ESCAPING_ENCLOSED,
		ENCLOSING_FIELD,
		UNENCLOSING_FIELD,
		READING_UNENCLOSED,
		FIELD_TERMINATED,
		LINE_TERMINATED,
		FILE_TERMINATED;
	}

	private static class Transition {
		State end;
		Predicate<Token> message;
		static Transition from(Predicate<Token> message, State end) {
			Transition out = new Transition();
			out.end = end;
			out.message = message;
			return out;
		}

	}

	/**
	 * thrown by the DelimitedParser.readLine() method when an error in the csv file is encountered.
	 * The parser is relatively strict and will not tolerate spurious white space outside quoted fields for example.
	 * @author terminological
	 *
	 */
	public static class MalformedCSVException extends Exception {
		public MalformedCSVException(State s, Token t) {
			super("State "+s+" cannot be followed by a "+t.toString());
		}
	}
	
	/**
	 * thrown by the DelimitedParser.readLine() method when the end of the file is reached
	 * @author terminological
	 *
	 */
	public static class EOFException extends Exception {}

	private static class Tokeniser {

		Matcher[] matchers;


		Tokeniser(String... strings) {
			matchers = new Matcher[strings.length];
			for (int i=0; i< strings.length ; i++) {
				matchers[i] = new Matcher(strings[i] == null ? "":strings[i]);
			}
		}

		Iterator<Token> tokenise(final Reader reader) {
			char[] buff = new char[1];

			return new Iterator<Token>() {

				Token next = null; //Do this with a queue
				boolean ready = false;

				@Override
				public boolean hasNext() {
					if (!ready) {
						try {
							read();
							ready = true;
						} catch (IOException e) {
						}
					}
					return ready;
				}

				@Override
				public Token next() {
					if (hasNext()) {
						ready = false;
						return next;
					} else {
						return new EOF();
					}
				}

				void read() throws IOException {
					StringBuilder out = new StringBuilder();
					while(true) {
						if(reader.read(buff, 0, 1) == -1) {
							next = new EOF();
							return;
						} else {
							boolean anyMatched = false;
							for (Matcher matcher :matchers) {
								Match m = matcher.consume(buff[0]);
								if (m.equals(Match.WHOLE)) {
									next = matcher;
									return;
								} else if (m.equals(Match.PART)) {
									anyMatched = true;
									out.append(buff[0]);
								} 
							}
							if (!anyMatched) {
								out.append(buff[0]);
								next = Token.fromString(out.toString());
								return;
							}
						}
					}
				}

			};
		}
	}

	private static interface Token {
		String get();
		boolean matched(String s);

		static Token fromString(String s) {
			return new Token() {
				@Override
				public String get() {
					return s;
				}
				@Override
				public boolean matched(String t) {
					return s.equals(t);
				}
				public String toString() {return "["+SourceCode.string(get())+"]";}
			};
		}

	}

	private static class EOF implements Token {
		@Override
		public String get() {
			return "";
		}

		@Override
		public boolean matched(String s) {
			return false;
		}

		public String toString() {return "<EOF>";}

	}

	private static class Matcher implements Token {

		char[] chars;
		int pos = 0;

		Matcher(String s) {
			chars = s.toCharArray();
		}

		public String get() {
			return String.valueOf(chars);
		}

		public boolean matched(String s) {
			return s.equals(get());
		}

		Match consume(char c) {
			if (chars[pos] == c) {
				pos +=1;
				if (pos == chars.length) {
					pos = 0;
					return Match.WHOLE;
				}
				return Match.PART;
			} else {
				pos = 0;
				return Match.NONE;
			}
		}
		public String toString() {return "["+SourceCode.string(get())+"]";}
	}

	private enum Match {
		WHOLE, PART, NONE
	}



}
