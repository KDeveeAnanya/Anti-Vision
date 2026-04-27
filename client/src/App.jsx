import { useState, useRef, useEffect } from 'react';

/**
 * App.jsx - The entire Anti-Vision frontend interface.
 * Built strictly following the premium, dark, atmospheric design specs.
 */
function App() {
  /**
   * 1. useState hooks:
   * useState is a React hook that lets us add state variables to our component.
   * When these variables change, React automatically re-renders the UI.
   * 
   * - status: Controls which view is currently visible ('IDLE', 'LOADING', 'RESULT', 'ERROR')
   * - input: Stores the text the user is typing into the textarea.
   * - response: Stores the JSON payload we get back from the Spring Boot backend.
   * - errorMsg: Stores human-readable error text if something fails.
   */
  const [status, setStatus] = useState('IDLE'); 
  const [input, setInput] = useState('');
  const [response, setResponse] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');

  // Ref to automatically focus the textarea when in IDLE state
  const textareaRef = useRef(null);

  useEffect(() => {
    if (status === 'IDLE' && textareaRef.current) {
      textareaRef.current.focus();
    }
  }, [status]);

  /**
   * 2. fetch() and async/await:
   * This function handles the form submission. It prevents the default page reload,
   * sets the state to LOADING, and makes an HTTP POST request to our Spring Boot API.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    setStatus('LOADING');
    setErrorMsg('');

    try {
      // The fetch() API is a modern browser feature for making network requests.
      const res = await fetch('http://localhost:8080/api/reflections', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ rawInput: input }),
      });

      if (!res.ok) {
        throw new Error('Failed to process reflection.');
      }

      // Parse the JSON returned by Spring Boot (our RuleResponse DTO)
      const data = await res.json();
      setResponse(data);
      
      // Transition to the result view
      setStatus('RESULT');

    } catch (err) {
      console.error(err);
      setErrorMsg('Something went wrong analyzing your entry. Are you sure the backend is running?');
      setStatus('ERROR');
    }
  };

  const handleReset = () => {
    setInput('');
    setResponse(null);
    setStatus('IDLE');
  };

  /**
   * 3. Conditional Rendering:
   * We use the `status` state variable to determine which UI block to render.
   * This allows us to keep everything on a single page without needing complex routing libraries.
   */
  return (
    <div className="relative min-h-screen bg-background text-textSoft flex items-center justify-center p-6">
      
      {/* Absolute positioning for atmospheric background layers */}
      <div className="absolute inset-0 bg-neural-grid opacity-30 pointer-events-none"></div>
      <div className="bg-scanline"></div>

      {/* Main Content Container */}
      <div className="relative z-10 w-full max-w-2xl mx-auto flex flex-col items-center">
        
        {/* =========================================
            STATE 1: INPUT VIEW ('IDLE')
            ========================================= */}
        {status === 'IDLE' && (
          <form 
            onSubmit={handleSubmit} 
            className="w-full flex flex-col items-center animate-stagger-reveal"
          >
            <h1 className="text-3xl md:text-5xl font-light text-center tracking-tight mb-2 text-white/90 drop-shadow-md">
              What do you never want to experience again?
            </h1>
            <p className="text-textMuted text-sm md:text-base font-light mb-12 tracking-wide">
              Be honest. This is just for you.
            </p>

            <div className="w-full relative group">
              {/* Subtle background glow effect */}
              <div className="absolute -inset-1 bg-accentViolet/10 rounded-xl blur-xl transition-all duration-1000 group-focus-within:bg-accentViolet/20 group-focus-within:blur-2xl"></div>
              
              <textarea
                ref={textareaRef}
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="I felt anxious about work, so I..."
                className="relative w-full h-40 md:h-56 bg-black/40 backdrop-blur-sm border border-white/5 rounded-xl p-6 text-lg md:text-xl font-light leading-relaxed placeholder:text-white/10 focus:border-accentViolet/30 focus:animate-glow-pulse transition-all duration-500"
              />
            </div>

            <button 
              type="submit" 
              disabled={!input.trim()}
              className="mt-12 px-8 py-3 rounded-full bg-accentViolet/10 text-accentViolet font-mono text-sm tracking-widest border border-accentViolet/20 hover:bg-accentViolet hover:text-white hover:shadow-[0_0_20px_rgba(124,58,237,0.5)] transition-all duration-300 disabled:opacity-30 disabled:hover:bg-accentViolet/10 disabled:hover:text-accentViolet disabled:hover:shadow-none"
            >
              INITIALIZE PROTOCOL
            </button>
          </form>
        )}

        {/* =========================================
            STATE 2: LOADING VIEW ('LOADING')
            ========================================= */}
        {status === 'LOADING' && (
          <div className="flex flex-col items-center justify-center animate-breathe">
            <p className="font-mono text-accentViolet tracking-widest uppercase">
              Reading your pattern...
            </p>
          </div>
        )}

        {/* =========================================
            STATE 3: ERROR VIEW ('ERROR')
            ========================================= */}
        {status === 'ERROR' && (
          <div className="flex flex-col items-center justify-center animate-stagger-reveal text-center">
            <p className="font-mono text-red-500 tracking-widest uppercase mb-4 opacity-80">
              System Failure
            </p>
            <p className="text-textMuted font-light mb-8">{errorMsg}</p>
            <button 
              onClick={handleReset}
              className="px-6 py-2 border border-white/10 rounded-full text-textSoft hover:bg-white/5 transition-colors font-mono text-xs"
            >
              REBOOT
            </button>
          </div>
        )}

        {/* =========================================
            STATE 4: RESULT VIEW ('RESULT')
            ========================================= */}
        {status === 'RESULT' && response && (
          <div className="w-full flex flex-col items-start w-full">
            
            {/* Human Story Sentence */}
            <div className="w-full mb-14 animate-stagger-reveal stagger-1 opacity-0">
              <p className="text-2xl md:text-3xl font-light leading-relaxed text-white/90">
                When you feel <span className="text-accentBlue font-normal">{response.emotion?.toLowerCase()}</span>, you tend to <span className="text-accentViolet font-normal">{response.trigger?.toLowerCase()}</span>, which leads to <span className="text-textMuted">{response.consequence?.toLowerCase()}</span>.
              </p>
            </div>

            {/* Preventive Rule Block */}
            <div className="w-full mb-12 animate-stagger-reveal opacity-0 [animation-delay:0.6s]">
              <h2 className="font-sans text-sm text-accentViolet tracking-widest mb-4 uppercase">Your Rule</h2>
              <p className="text-3xl md:text-4xl font-light leading-tight text-white drop-shadow-md">
                {response.preventiveRule?.replace(/IF\s+/i, '')?.replace(/,?\s*THEN\s+/i, ', ')}
              </p>
            </div>

            {/* Early Warning Block */}
            <div className="w-full mb-16 animate-stagger-reveal opacity-0 [animation-delay:1s]">
              <h2 className="font-sans text-sm text-textMuted tracking-widest mb-3 uppercase">Watch for this</h2>
              <p className="text-lg md:text-xl font-light leading-relaxed text-textMuted italic">
                {response.earlyWarning?.replace(/^Watch for\s+/i, '')}
              </p>
            </div>

            <button 
              onClick={handleReset}
              className="self-center px-6 py-2 border border-white/10 rounded-full text-textMuted hover:text-white hover:bg-white/5 hover:border-white/20 transition-all duration-300 font-mono text-xs tracking-widest animate-stagger-reveal opacity-0 [animation-delay:1.5s]"
            >
              REFLECT AGAIN
            </button>
          </div>
        )}

      </div>
    </div>
  );
}

export default App;
