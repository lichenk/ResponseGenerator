# ResponseGenerator
Machine learning classifiers for email response prediction and generation on the Enron corpus

10601 Machine Learning Project

Peijin Zhang, Ananya Kumar, Li Chen Koh, Bojian Han

——————— Task Pipeline ———————

1. Understand the emails
- how to identify structure “email A is response to email B”

2. Get relations between features
- Output should be a set of email classes
- Each email has sender ID, title, body, and list of children emails 

3. Extract features from email
- Given set of emails and relations
- For each email what features should we extract? How do we extract them

4. Learn
- Given emails and relations + features for each email, learn trends (predict if an email has a response)
- Explore libraries (weka, R, etc)
