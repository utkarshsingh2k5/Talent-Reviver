import React, { useState, useEffect, useContext } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { usercontext } from '../appcontext';
import Styles from './candidateRankings.module.css';

function CandidateRankings() {
    const { jobId } = useParams();
    const navigate = useNavigate();
    const { backendURL } = useContext(usercontext);
    const [candidates, setCandidates] = useState([]);
    const [selectedCandidates, setSelectedCandidates] = useState([]);
    const [isMatching, setIsMatching] = useState(false);
    const [isSending, setIsSending] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedCandidate, setSelectedCandidate] = useState(null);

    const fetchMatches = async () => {
        setIsLoading(true);
        try {
            const response = await fetch(`${backendURL}/hr/matching/${jobId}/matches`, {
                credentials: 'include'
            });
            if (response.ok) {
                const data = await response.json();
                setCandidates(data);
            }
        } catch (error) {
            toast.error('Failed to fetch candidate rankings');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchMatches();
    }, [jobId]);

    const handleStartMatching = async () => {
        setIsMatching(true);
        try {
            const response = await fetch(`${backendURL}/hr/matching/${jobId}/match`, {
                method: 'POST',
                credentials: 'include'
            });
            if (response.ok) {
                const text = await response.text();
                toast.success(text);
                await fetchMatches();
            } else {
                toast.error('AI Matching failed to start');
            }
        } catch (error) {
            toast.error('Network error occurred');
        } finally {
            setIsMatching(false);
        }
    };

    const toggleCandidateSelection = (email) => {
        setSelectedCandidates(prev =>
            prev.includes(email) ? prev.filter(e => e !== email) : [...prev, email]
        );
    };

    const handleSendOutreach = async () => {
        if (selectedCandidates.length === 0) {
            toast.warn('Please select at least one candidate');
            return;
        }

        setIsSending(true);
        try {
            const response = await fetch(`${backendURL}/hr/jobs/${jobId}/outreach`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ candidateEmails: selectedCandidates }),
                credentials: 'include'
            });

            if (response.ok) {
                const text = await response.text();
                toast.success(text);
                setSelectedCandidates([]);
                // Optional: navigate to tracker to see progress
                // navigate(`/hr/outreach/${jobId}`);
            } else {
                const errorText = await response.text();
                toast.error(`Outreach failed: ${errorText}`);
            }
        } catch (error) {
            toast.error('Network error occurred');
        } finally {
            setIsSending(false);
        }
    };

    return (
        <div className={Styles.container}>
            <div className={Styles.header}>
                <button className={Styles.backBtn} onClick={() => navigate('/hr/dashboard')}>← Back to Dashboard</button>
                <div className={Styles.titleSection}>
                    <h1>Candidate Rankings</h1>
                    <p>Job ID: {jobId}</p>
                </div>
                <div className={Styles.actionButtons}>
                    <button className={Styles.matchBtn} onClick={handleStartMatching} disabled={isMatching}>
                        {isMatching ? 'AI Matching in Progress...' : '⚡ Start Rediscovery'}
                    </button>
                    {selectedCandidates.length > 0 && (
                        <button className={Styles.outreachBtn} onClick={handleSendOutreach} disabled={isSending}>
                            {isSending ? 'Sending...' : `Send Outreach (${selectedCandidates.length})`}
                        </button>
                    )}
                </div>
            </div>

            {isLoading ? (
                <div className={Styles.loader}>Loading rankings...</div>
            ) : (
                <div className={Styles.mainContent}>
                    <table className={Styles.rankTable}>
                        <thead>
                            <tr>
                                <th style={{ width: '40px' }}>Select</th>
                                <th>Rank</th>
                                <th>Candidate</th>
                                <th>Score</th>
                                <th>Recommendation</th>
                                <th>Exp Match</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {candidates.length > 0 ? candidates.map((c, index) => (
                                <tr key={c.candidateEmail} className={Styles.row}>
                                    <td>
                                        <input
                                            type="checkbox"
                                            checked={selectedCandidates.includes(c.candidateEmail)}
                                            onChange={() => toggleCandidateSelection(c.candidateEmail)}
                                        />
                                    </td>
                                    <td>{index + 1}</td>
                                    <td><strong>{c.candidateName}</strong><br/><small>{c.candidateEmail}</small></td>
                                    <td><span className={Styles.scoreBadge}>{c.matchScore}%</span></td>
                                    <td><span className={`${Styles.recBadge} ${Styles[c.recommendation?.toLowerCase().replace('_', '')]}`}>{c.recommendation}</span></td>
                                    <td>{c.experienceMatch ? '✅' : '❌'}</td>
                                    <td>
                                        <button className={Styles.viewBtn} onClick={() => setSelectedCandidate(c)}>View AI Analysis</button>
                                    </td>
                                </tr>
                            )) : (
                                <tr><td colSpan="7" className={Styles.noData}>No candidates matched yet. Click "Start Rediscovery" to find candidates.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {selectedCandidate && (
                <div className={Styles.modalOverlay}>
                    <div className={Styles.modal}>
                        <div className={Styles.modalHeader}>
                            <h2>AI Analysis: {selectedCandidate.name}</h2>
                            <button className={Styles.closeBtn} onClick={() => setSelectedCandidate(null)}>×</button>
                        </div>
                        <div className={Styles.modalBody}>
                            <div className={Styles.analysisSection}>
                                <h3>Matching Reasoning</h3>
                                <p className={Styles.reasonText}>{selectedCandidate.reason}</p>
                            </div>
                            <div className={Styles.skillsGrid}>
                                <div className={Styles.skillBox}>
                                    <h4>Matching Skills</h4>
                                    <ul>{selectedCandidate.matchingSkills.map((s, i) => <li key={i}>{s}</li>)}</ul>
                                </div>
                                <div className={Styles.skillBox}>
                                    <h4>Missing Skills</h4>
                                    <ul>{selectedCandidate.missingSkills.map((s, i) => <li key={i}>{s}</li>)}</ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default CandidateRankings;
