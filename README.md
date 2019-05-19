IOS team follows the below listed process:

- They keep the version name to be the release version name. For example 2.5.0. And keep incrementing version code for every subsequent Qa builds and production build.
- They create a new branch for every Qa and prod releases. For example suppose the first Qa build version code is 30, they will have branches as follows RC_2.5.0.30, RC_2.5.0.31 etc.
- This has lot of overhead but helps in applying patches to particular version of Qa build.


We will use the following process for android

- We use git flow with support for following branches: 
 - Master - Which will have latest production commit
 - Develop - This will be our integration branch
 - Release - We start a release branch once the features for release scope is merged to develop. We create one release branch and keep on making fixes for bugs which surface during Qa testing on this branch. For subsequent apk's we take out, we increment the version code and we tag this branch with the RC_<version-name>.<version-code> tag template.
 - Feature - we use git flow feature start RA-XXXX to create a feature branch. When done with the development we use git flow feature finish RA-XXXX. 
 - bugfix - we use this for non production bug fix branches. Commands: git flow bugfix start RA-XXXX [Note: Bugfix is not available in git flow tool, use git-flow-avh tool]
 - hotfix - this should be used for hot patches on master for production bugs
