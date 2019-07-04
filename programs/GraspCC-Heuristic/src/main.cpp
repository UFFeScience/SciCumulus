
#include "estrutures.h"

int main(int argc, char *argv[]){
    srand(123456+time(NULL));
    unsigned long int time_ini;
    double time_total;
    double fnew, fant=999999999;
    int np=0, tm, tm_best=99999999;
    int **solution, **solution_best;
    string instance;
    srand(rand()%time(NULL));

    if(argc!=4){
        cout<<"Incorrect Parameters Number."<<endl;
        exit(1);
    }


    TpackageList list;

    TuserRequest userRequest;

    time_t inicio, fim;

    instance=argv[1];

    userRequest.alpha1=atof(argv[2]);

    userRequest.alpha2=atof(argv[3]);

    read_Instance(instance, &list, &userRequest);

    // Initialize the solution
    solution=(int**)malloc(sizeof(int*)*list.n);
    for(int p=0; p < list.n; p++){
	solution[p]=(int*)malloc(sizeof(int)*userRequest.nPackage);
	for(int i=0; i < userRequest.nPackage; i++){
		solution[p][i]=0;
	}
    }

    time_ini = (unsigned long int) clock();
    inicio=time(NULL);

    //optimizationModel (instance, list, userRequest, userRequest.alpha1, userRequest.alpha2, solution);
    //time_total = calculate_time(time_ini, clock());
    //fim=time(NULL);
    //cout<<"Total execution time (clock) = " << time_total << endl;
    //cout<<"Time execution time = " << difftime(fim,inicio)<< endl;


    // Initialize the best solution
    solution_best=(int**)malloc(sizeof(int*)*list.n);
	for(int p=0; p < list.n; p++){
		solution_best[p]=(int*)malloc(sizeof(int)*userRequest.nPackage);
		for(int i=0; i < userRequest.nPackage; i++){
			solution_best[p][i]=0;
		}
	}
    price_max=-1;
	for(int p=0; p < list.n; p++){
		if(list.package[p].price>price_max)
			price_max=list.package[p].price;
	}

	time_ini = (unsigned long int) clock();
	inicio=time(NULL);

	double factual=999999999;

	int it=0, itt=0;
	while (it<50){

		heuristic (&list, userRequest, userRequest.alpha1, userRequest.alpha2, np, &tm, it, solution);
		//getchar();

		fnew=calculate_cost_function(&list,userRequest,userRequest.alpha1,userRequest.alpha2,solution,tm);

		//time_total = calculate_time(time_ini, clock());

		//cout<<"Heuristic execution time = " << time_total << endl;

		if (DEBUG)
			cout<< "[it= " << it << "] Function cost= " << fnew << endl;

		local_search(&list, userRequest, userRequest.alpha1, userRequest.alpha2, solution, &tm, &fnew, &fant);

		if(improved(factual,fnew)){
			copy_solution(list, userRequest,list.n, userRequest.nPackage,solution_best, solution);
			factual=fnew;
			tm_best=tm;
			it=0;
		}
		it++;
		time_total = calculate_time(time_ini, clock());
		cout << "Function Cost =" << factual <<  " it = " << itt << " time = " << time_total << endl;
		itt++;
		fant=999999999;

        }

	double cost_t=0.0;
	for(int p=0; p < list.n; p++){

		for(int i=0; i < userRequest.nPackage; i++){
			for(int ii=0; ii < i; ii++){
				if(solution_best[p][ii]==0){
					solution_best[p][ii]=solution_best[p][i];
					solution_best[p][i]=0;
					break;
				}
			}
		}

	}

	for(int p=0; p < list.n; p++){
		cout << "Package [" << p << "]" << endl;
		for(int i=0; i < userRequest.nPackage; i++){
			if (solution_best[p][i]!=0){

				cout << "["<< i << "] = " << solution_best[p][i] << endl;
			}
			cost_t+=solution_best[list.package[p].id][i]*list.package[p].price;
		}

	}

	 cout<< "Function cost= " << factual << endl;
	 cout << "[Best] Maximum time = " << tm_best << " hours" << endl;
	 cout << "[Best] Monetary Cost = $" << cost_t << endl;

	 fim=time(NULL);
	 time_total = calculate_time(time_ini, clock());

	 cout<<"Total execution time (clock) = " << time_total << endl;
	 cout<<"Time execution time = " << difftime(fim,inicio)<< endl;

	 //time_ini = (unsigned long int) clock();
	 //inicio=time(NULL);
	 //optimizationModel (instance, list, userRequest, userRequest.alpha1, userRequest.alpha2, solution_best);
	 //time_total = calculate_time(time_ini, clock());
	// fim=time(NULL);
	 //cout<<"Total execution time (clock) = " << time_total << endl;
	// cout<<"Time execution time = " << difftime(fim,inicio)<< endl;
    return 0;
}
