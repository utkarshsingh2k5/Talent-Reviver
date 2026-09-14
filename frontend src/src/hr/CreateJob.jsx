import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { usercontext } from '../appcontext';
import Styles from './createJob.module.css';

function CreateJob() {
    const navigate = useNavigate();
    const { backendURL } = useContext(usercontext);
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        requiredSkills: '',
        preferredSkills: '',
        minExperience: '',
        maxExperience: '',
        location: '',
        employmentType: 'FULL_TIME'
    });
    const [isLoading, setIsLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        try {
            // Convert comma-separated strings to arrays for the backend
            const processedData = {
                ...formData,
                requiredSkills: formData.requiredSkills.split(',').map(s => s.trim()).filter(s => s !== ''),
                preferredSkills: formData.preferredSkills ? formData.preferredSkills.split(',').map(s => s.trim()).filter(s => s !== '') : []
            };

            const response = await fetch(`${backendURL}/hr/jobs`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(processedData),
                credentials: 'include'
            });

            if (response.ok) {
                toast.success('Job posting created successfully!');
                navigate('/hr/dashboard');
            } else {
                const errorData = await response.text();
                toast.error(`Failed to create job: ${errorData}`);
            }
        } catch (error) {
            toast.error('Network error occurred');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={Styles.container}>
            <div className={Styles.formCard}>
                <div className={Styles.header}>
                    <h1>Create Job Posting</h1>
                    <p>Define the requirements for your ideal candidate</p>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className={Styles.inputGroup}>
                        <label>Job Title</label>
                        <input name="title" value={formData.title} onChange={handleChange} placeholder="e.g. Senior Java Developer" required />
                    </div>

                    <div className={Styles.inputGroup}>
                        <label>Job Description (JD)</label>
                        <textarea name="description" value={formData.description} onChange={handleChange} placeholder="Detailed responsibilities and requirements..." required rows="6" />
                    </div>

                    <div className={Styles.row}>
                        <div className={Styles.inputGroup}>
                            <label>Required Skills</label>
                            <input name="requiredSkills" value={formData.requiredSkills} onChange={handleChange} placeholder="e.g. Java, Spring Boot, MySQL" required />
                        </div>
                        <div className={Styles.inputGroup}>
                            <label>Preferred Skills</label>
                            <input name="preferredSkills" value={formData.preferredSkills} onChange={handleChange} placeholder="e.g. AWS, Docker, Kubernetes" />
                        </div>
                    </div>

                    <div className={Styles.row}>
                        <div className={Styles.inputGroup}>
                            <label>Min Experience (Years)</label>
                            <input type="number" name="minExperience" value={formData.minExperience} onChange={handleChange} placeholder="0" required />
                        </div>
                        <div className={Styles.inputGroup}>
                            <label>Max Experience (Years)</label>
                            <input type="number" name="maxExperience" value={formData.maxExperience} onChange={handleChange} placeholder="10" required />
                        </div>
                    </div>

                    <div className={Styles.row}>
                        <div className={Styles.inputGroup}>
                            <label>Location</label>
                            <input name="location" value={formData.location} onChange={handleChange} placeholder="e.g. Remote, New York, NY" required />
                        </div>
                        <div className={Styles.inputGroup}>
                            <label>Employment Type</label>
                            <select name="employmentType" value={formData.employmentType} onChange={handleChange}>
                                <option value="FULL_TIME">Full Time</option>
                                <option value="PART_TIME">Part Time</option>
                                <option value="CONTRACT">Contract</option>
                                <option value="INTERNSHIP">Internship</option>
                            </select>
                        </div>
                    </div>

                    <div className={Styles.footer}>
                        <button type="button" className={Styles.cancelBtn} onClick={() => navigate('/hr/dashboard')}>Cancel</button>
                        <button type="submit" className={Styles.submitBtn} disabled={isLoading}>
                            {isLoading ? 'Creating...' : 'Create Job Posting'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default CreateJob;
