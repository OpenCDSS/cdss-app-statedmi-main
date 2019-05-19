# doc-split-notes

These are notes to split StateDMI legacy Word/PDF documentation
out of the main repository and into a separate repository.
This will allow the main repository to focus on the code.

## Use `git filter-branch` ##

Follow the instructions
[Splitting a subfolder out into a new repository](https://help.github.com/en/articles/splitting-a-subfolder-out-into-a-new-repository),
with similar numbered steps.

1. Open Git Bash.
2. Change to the location where want to create the new repository, in this case under `cdss-dev/StateDMI/git-repos`.
3. Clone the repository that contains the subfolder.
An existing `cdss-app-statedmi-main` repository folder exists.
However, given that the contents of the repo will be modified, clone a new repository with:
`git clone https://github.com/OpenCDSS/cdss-app-statedmi-main.git cdss-app-statedmi-doc`.
This is similar to how TSTool's repositories are named.
4. Change the current working directory to the cloned repository:  
`cd cdss-app-statedmi-doc`.
5. Filter out all but the desired folder:  `git filter-branch --prune-empty --subdirectory-filter doc master`
This moves the folders under `doc` to the top folders.
Therefore, make a folder `doc` and move the other folders under that`.  Commit.
6. Create a new repository on GitHub in the OpenCDSS account.  Name it `cdss-app-statedmi-doc`.
7. Use the GitHub copy feature to copy the repository URL.
8. Check the existing remote name with:  `git remote -v`, which shows:
```
origin  https://github.com/OpenCDSS/cdss-app-statedmi-main.git (fetch)
origin  https://github.com/OpenCDSS/cdss-app-statedmi-main.git (push)
```
9. Set the new remote URL:  `git remote set-url origin https://github.com/OpenCDSS/cdss-app-statedmi-doc.git`
10. Verify that the new remote URL has been set:  `git remote -v`, which shows:
```
origin  https://github.com/OpenCDSS/cdss-app-statedmi-doc.git (fetch)
origin  https://github.com/OpenCDSS/cdss-app-statedmi-doc.git (push)
```
11. Push the changes to the new repository to GitHub:  `git push -u origin master`
12. **The above does not seem to retain the history of the folder, which is bad so set it aside.**
	1. Rename the remote repository in GitHub to `cdss-app-statedmi-doc-attempt1`.
	2. Set the new remote URL:  `git remote set-url origin https://github.com/OpenCDSS/cdss-app-statedmi-doc-attempt1.git`
	3. Verify the result:  `git remote -v`, which shows:
```
origin  https://github.com/OpenCDSS/cdss-app-statedmi-doc-attempt1.git (fetch)
origin  https://github.com/OpenCDSS/cdss-app-statedmi-doc-attempt1.git (push)
```
13. Rename the local repository from `cdss-app-statedmi-doc` to `x-cdss-app-statedmi-doc-attempt1`

## Use `git subtree split` Command ##

An alternate method for splitting a repository is to use `git subtree split`.  See:

* [Using Git subtrees for repository separation](https://makingsoftware.wordpress.com/2013/02/16/using-git-subtrees-for-repository-separation/).
* [`git subtree`](https://manpages.debian.org/testing/git-man/git-subtree.1.en.html)

Do the following in Git Bash in `StateDMI/git-repos`:

1. Initialize a new bare repository:
	1. `mkdir cdss-app-statedmi-doc`
	2. `cd cdss-app-statedmi-doc`
	3. `git init --bare`
2. Using the GitHub website, create a new remote repository named `cdss-app-statedmi-doc`, with no README.
3. In the repository `cdss-app-statedmi-main`, split the shared code into a separate branch:
	1. `git subtree split --prefix=doc -b doc-split`
	2. This takes awhile to run.  The branch will have all the commit history.
	Don't squash because want to keep the history.
4. From the same `cdss-app-statedmi-main` repository folder,
push the new branch to the new local repository:
	1. `git push ../cdss-app-statedmi-doc doc-split:master`
5. From the new split repository, push the comit to the new remote shared repository:
	1. `cd ../cdss-app-statedmi-doc`
	2. `git remote add origin https://github.com/OpenCDSS/cdss-app-statedmi-doc.git`
	3. `git push origin master`
	4. GitHub shows the documentation files.  However, they are not in a `doc` folder.
	The local repository also contains a bare repository which is the `.git` contents.
6. Reclone to get the standard repository.
	1. Move `cdss-app-statedmi-doc` to `x-cdss-app-statedmi-doc-bare`
	2. Reclone:  `git clone https://github.com/OpenCDSS/cdss-app-statedmi-doc.git`

Actually, this does not show any history either.
Reviewing the original code shows that there is only one commit in 2016.
I must not have committed the documentation history when migrating into Git.

## Split the StateCU Developer Documentation ##

Follow the instructions
[Splitting a subfolder out into a new repository](https://help.github.com/en/articles/splitting-a-subfolder-out-into-a-new-repository),
with similar numbered steps.

1. Open Git Bash.
2. Change to the location where want to create the new repository, in this case under `cdss-dev/StateCU/git-repos`.
3. Clone the repository that contains the subfolder.
An existing `cdss-app-statecu-fortran` repository folder exists.
However, given that the contents of the repo will be modified, clone a new repository with:
`git clone https://github.com/OpenCDSS/cdss-app-statecu-fortran.git cdss-app-statecu-fortran-doc-dev`.
This is similar to how StateMod's repositories are named.
4. Change the current working directory to the cloned repository:  
`cd cdss-app-statecu-fortran-doc-dev`.
5. Filter out all but the desired folder:  `git filter-branch --prune-empty --subdirectory-filter doc-dev-mkdocs-project master`
This moves the folders under `doc-dev-mkdocs-project` to the top folder in the result.
Therefore, make a folder `mkdocs-project` and move the other folders under that`.
Also add `.gitattributes`, `.gitignore`, `README.md`, and `LICENSE.md` files, copied and modified from StateMod. Commit.
6. Create a new repository on GitHub in the OpenCDSS account.  Name it `cdss-app-statecu-fortran-doc-dev`.
7. Check the existing remote name with:  `git remote -v`, which shows:
```
origin  https://github.com/OpenCDSS/cdss-app-statecu-main.git (fetch)
origin  https://github.com/OpenCDSS/cdss-app-statecu-main.git (push)
```
8. Set the new remote URL:  `git remote set-url origin https://github.com/OpenCDSS/cdss-app-statecu-fortran-doc-dev.git`
9. Verify that the new remote URL has been set:  `git remote -v`, which shows:
```
origin  https://github.com/OpenCDSS/cdss-app-statedmi-doc.git (fetch)
origin  https://github.com/OpenCDSS/cdss-app-statedmi-doc.git (push)
```
10. Push the changes to the new repository to GitHub:  `git push -u origin master`
11. Definitely lost history.
	1. Rename to `cdss-app-statecu-fortran-doc-dev-attempt1` in GitHub.
	2. `git remote set-url origin https://github.com/OpenCDSS/cdss-app-statecu-fortran-doc-dev-attempt1.git`
	3. Rename repository folder to x-cdss-app-statecu-fortran-doc-dev-attempt1

Try the second way to split a repository and see if it retains the history:

Do the following in Git Bash in `StateCU/git-repos`:

1. Initialize a new bare repository:
	1. `mkdir cdss-app-statecu-fortran-doc-dev`
	2. `cd cdss-app-statedmi-doc`
	3. `git init --bare`
2. Using the GitHub website, create a new remote repository named `cdss-app-statedmi-doc`, with no README.

3. In the repository `cdss-app-statecu-fortran`, split the shared code into a separate branch:
	1. `git subtree split --prefix=doc-dev-mkdocs-project -b doc-split`
	2. This takes awhile to run.  The branch will have all the commit history.
	Don't squash because want to keep the history.
4. From the same `cdss-app-statecu-fortran` repository folder,
push the new branch to the new local repository:
	1. `git push ../cdss-app-statecu-fortran-doc-dev doc-split:master`
5. From the new split repository, push the commit to the new remote shared repository:
	1. `cd ../cdss-app-statecu-fortran-doc-dev`
	2. `git remote add origin https://github.com/OpenCDSS/cdss-app-statecu-fortran-doc-dev.git`
	3. `git push origin master`
	4. GitHub shows the documentation files and has retained the history!
	However, they are not in a folder as in the previous repository.
	The local repository also contains a bare repository which is the `.git` contents.
6. Reclone to get the standard repository.
	1. Move `cdss-app-statecu-fortran-doc-dev` to `x-cdss-app-statecu-fortran-doc-dev-bare`
	2. Reclone:  `git clone https://github.com/OpenCDSS/cdss-app-statecu-fortran-doc-dev.git`
7. Clean up the repository:
	1. Add a top-level `mkdocs-project` folder and move folders into that.
	2. Copy `.gitignore`, `.gitattributes`, `README.md` and `LICENSE.md` from "attempt1" files.

## Split out the StateDMI tests ##

Do the following in Git Bash in `StateDMI/git-repos`:

0. Rename the previous `cdss-app-statedmi-test` to `x-cdss-app-statedmi-test`.
1. Initialize a new bare repository:
	1. `mkdir cdss-app-statedmi-test`
	2. `cd cdss-app-statedmi-test`
	3. `git init --bare`
2. Using the GitHub website, create a new remote repository named `cdss-app-statedmi-test`, with no README.
3. In the repository `cdss-app-statedmi-main`, split the shared code into a separate branch:
	1. `git subtree split --prefix=test -b test-split`
	2. This takes awhile to run.  The branch will have all the commit history.
	Don't squash because want to keep the history.
	3. Output is:  `Created branch \`test-split\``

4. From the same `cdss-app-statedmi-main` repository folder,
push the new branch to the new local repository:
	1. `git push ../cdss-app-statedmi-test test-split:master`
```
git push ../cdss-app-statedmi-test test-split:master
Counting objects: 3193, done.
Delta compression using up to 8 threads.
Compressing objects: 100% (1407/1407), done.
Writing objects: 100% (3193/3193), 3.25 MiB | 5.89 MiB/s, done.
Total 3193 (delta 1635), reused 3159 (delta 1632)
remote: Resolving deltas: 100% (1635/1635), done.
To ../cdss-app-statedmi-test
 * [new branch]      test-split -> master
```
5. From the new split repository, push the commit to the new remote shared repository:
	1. `cd ../cdss-app-statedmi-test`
	2. `git remote add origin https://github.com/OpenCDSS/cdss-app-statedmi-test.git`
	3. `git push origin master`
	4. GitHub shows the test files and has retained the history!
	However, they are not in a folder as in the previous repository.
	The local repository also contains a bare repository which is the `.git` contents.
6. Reclone to get the standard repository.
	1. Move `cdss-app-statedmi-test` to `x-cdss-app-statedmi-test-bare`
	2. Reclone:  `git clone https://github.com/OpenCDSS/cdss-app-statedmi-test.git`
7. Clean up the repository:
	1. Add a top-level `test` folder and move folders into that to match the previous folder structure.
	2. Copy `.gitignore`, `.gitattributes`, `README.md` and `LICENSE.md` from other repos.
	3. Commit to the repo.
