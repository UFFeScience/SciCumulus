
#define _EM_ESTRUTURAS
#include "estrutures.h"

using namespace std;

double total_ram, total_hd, total_flops;
double *t_ram, *t_hd;
int num_selected_package, enter, useHeuristicSolution;
double min1, max1, min2, max2, maxx, max_price;
int *pack_selected, *pack_ram, *pack_disk, pr=0, ph=0, pack, pack_hd;


double calculate_time(unsigned long int ini, unsigned long int end)
{
  double r;

  if(end >= ini)
    r = ((double)(end - ini)) / CLOCKS_PER_SEC;
  else
    r = ((double)( (end + (unsigned long int)-1) - ini)) / CLOCKS_PER_SEC;
  return r;
}

void read_Instance(string instanceFile, TpackageList* list, TuserRequest* userRequest){

	const int MAX=1000; char buff[MAX];

	ifstream fin(instanceFile.c_str()); //Open the file to reading in text mode
	if (!fin.is_open())
	{
		std::cerr << "File of the instance "<< instanceFile << " not found." << std::endl;
		exit(1);
	}

    int line = 1;
	for (;;)
	{
        buff[0] = '\0';
		fin.getline(buff,MAX);          // Read a line of the instance file
        if (fin.eof()) break;
		char *first= strtok(buff," ");  // Divide the line for the 'space' character
		if(first == NULL){
			line++;
			continue;
		}
		char *second;
		if (strcmp(first, "package") == 0){
			second=strtok(NULL,"\0"); // Get the value
			list->n=atoi(second);
			for(int i=0; i< list->n;i++){
				Tpackage package;
				package.id=i;
				package.disk=0.0;
				package.gflops=0.0;
				package.plataform= _64BITS;
				package.price=0.0;
				package.ram=0.0;
				list->package.push_back(package);
			}
		}
		else if (strcmp(first, "Gflops") == 0){
			for(int i=0; i< list->n;i++){
			    if(i==list->n-1)
			    	second=strtok(NULL,"\0"); // Get the value
			    else
			    	second=strtok(NULL," ");  // Get the value
			    list->package[i].gflops=atof(second)*3600;

			}
		}
		else if (strcmp(first, "ram") == 0){
			for(int i=0; i< list->n;i++){
				if(i==list->n-1)
					second=strtok(NULL,"\0"); // Get the value
				else
					second=strtok(NULL," ");  // Get the value
				list->package[i].ram=atof(second);

			}
		}
		else if (strcmp(first, "plataform") == 0){
			for(int i=0; i< list->n;i++){
				if(i==list->n-1)
					second=strtok(NULL,"\0"); // Get the value
				else
					second=strtok(NULL," ");  // Get the value
				if(atoi(second)==64)
					list->package[i].plataform = _64BITS;
				else if(atoi(second)==32)
					list->package[i].plataform = _32BITS;
			}
		}
		else if (strcmp(first, "disk") == 0){
			for(int i=0; i< list->n;i++){
				if(i==list->n-1)
					second=strtok(NULL,"\0"); // Get the value
				else
					second=strtok(NULL," ");  // Get the value
				list->package[i].disk=atof(second);

			}
		}
		else if (strcmp(first, "price") == 0){
			for(int i=0; i< list->n;i++){
				if(i==list->n-1)
					second=strtok(NULL,"\0"); // Get the value
				else
					second=strtok(NULL," ");
				list->package[i].price=atof(second);

			}
		}
		else if (strcmp(first, "C") == 0){
			second=strtok(NULL,"\0"); // Get the value
			userRequest->cost=atof(second);

		}
		else if (strcmp(first, "F") == 0){
			second=strtok(NULL,"\0"); // Get the value
			userRequest->gflops=atof(second);

		}
		else if (strcmp(first, "M") == 0){
			second=strtok(NULL,"\0"); // Get the value
			userRequest->ram=atof(second);

		}
		else if (strcmp(first, "D") == 0){
			second=strtok(NULL,"\0"); // Get the value
			userRequest->disk=atof(second);

		}
		else if (strcmp(first, "T") == 0){
			second=strtok(NULL,"\0"); // Get the value
			userRequest->timeMax=atof(second);

		}
		else if (strcmp(first, "P") == 0){
			second=strtok(NULL,"\0"); // Get the value
			userRequest->nPackage=atof(second);

		}
		else if (first[0] != '#')
		{
			std::cerr << "ERROR: Unknown parameter in the line " << line << "." << std::endl;
			exit(4);
		}
		line++;

	}

	cout << "Package Number = " << list->n << endl;
	for(int i=0; i< list->n;i++){
		cout << "package(" << i << ")" << endl;
		cout << "Gflops = " << list->package[i].gflops << endl;
		cout << "RAM = " << list->package[i].ram << endl;
		cout << "Disk = " << list->package[i].disk << endl;
		cout << "Price = " << list->package[i].price << endl;
	}
	cout << "User Request " << endl;
	cout << "Gflops = " << userRequest->gflops << endl;
	cout << "RAM = " << userRequest->ram << endl;
	cout << "Disk = " << userRequest->disk << endl;
	cout << "Maximum Cost = " << userRequest->cost << endl;
	cout << "Maximum Time = " << userRequest->timeMax << endl;
	cout << "Maximum Package Number = " << userRequest->nPackage << endl;

	 t_ram= (double*)malloc(sizeof(double)*2*userRequest->timeMax);
	 t_hd= (double*)malloc(sizeof(double)*2*userRequest->timeMax);

	min1=99999; max1=-1; min2=99999; max2=-1;
	max_price=-1;
	pack_selected=(int*)malloc(sizeof(int)*list->n);
	for(int p=0; p < list->n; p++){
		pack_selected[p]=0;
		if(list->package[p].price*(userRequest->gflops/list->package[p].gflops)<min1)
			min1=list->package[p].price*(userRequest->gflops/list->package[p].gflops);
		if((userRequest->gflops/list->package[p].gflops)/userRequest->nPackage<min2)
			min2=(userRequest->gflops/list->package[p].gflops)/userRequest->nPackage;
		if(list->package[p].price*(userRequest->gflops/list->package[p].gflops)>max1)
			max1=list->package[p].price*(userRequest->gflops/list->package[p].gflops);
		if((userRequest->gflops/list->package[p].gflops)/userRequest->nPackage>max2){
			max2=(userRequest->gflops/list->package[p].gflops)/userRequest->nPackage;
			max_price=list->package[p].price;
		}
	}
	// Find the  maximum prices of the packages
	maxx=-1;
	for(int p=0; p < list->n; p++){
		if(list->package[p].price>maxx){
			maxx=list->package[p].price;
			//max2=(userRequest->gflops/list->package[p].gflops)/userRequest->nPackage;
		}
	}

	double aux1=userRequest->alpha1 , aux2=userRequest->alpha2;
	userRequest->alpha1= aux1/(userRequest->timeMax*userRequest->nPackage*maxx);
	userRequest->alpha2= aux2/userRequest->timeMax;
	// Initialize the packages
	double mem_max=-1, disk_max=-1;
        pack_ram=(int*)malloc(sizeof(int)*list->n);
        pack_disk=(int*)malloc(sizeof(int)*list->n);
	for(int p=0; p < list->n; p++){
		pack_ram[p]=-1;
		pack_disk[p]=-1;
		if(list->package[p].ram > mem_max){
			mem_max = list->package[p].ram;
			pack=list->package[p].id;
		}
		if(list->package[p].disk > disk_max){
			disk_max = list->package[p].disk;
			pack_hd=list->package[p].id;
		}

		// Calculate the objective function
		list->package[p].fo = userRequest->alpha1*(list->package[p].price*(userRequest->gflops/list->package[p].gflops)) + userRequest->alpha2*((userRequest->gflops/list->package[p].gflops)/userRequest->nPackage);
		list->package[p].fo = userRequest->alpha1*(userRequest->timeMax*userRequest->nPackage*maxx)*(list->package[p].price*(userRequest->gflops/list->package[p].gflops)) + userRequest->alpha2*userRequest->timeMax*((userRequest->gflops/list->package[p].gflops)/userRequest->nPackage);
	}

	// Sort in increasing order the objetive cost
	sort(list->package.begin(), list->package.end(), &compare_fo);
	pr=0; ph=0;
	for(int p=0; p < list->n; p++){
		if(list->package[p].ram*userRequest->nPackage >= userRequest->ram){
			pack_ram[pr]=p; pr++;
		}
		if(list->package[p].disk*userRequest->nPackage >= userRequest->disk){
			pack_disk[ph]=p; ph++;
		}
	}
	for(int pp=0; pp< list->n ; pp++){
		if(pack== list->package[pp].id){
			pack=pp;
			break;
		}
	}
	for(int pp=0; pp< list->n ; pp++){
		if(pack_hd== list->package[pp].id){
			pack_hd=pp;
			break;
		}
	}

}

/*int optimizationModel (string instanceFile, TpackageList list, TuserRequest userRequest, double alpha1, double alpha2, int **solution){
	// Create a UFFLP problem instance
	UFFProblem *prob=UFFLP_CreateProblem();

	unsigned long int time_ini = 0;
	double time_total;
	int writeLP=1, writeLog=1, executionTimeMIP_seg=60;
	time_t inicio, fim;
	inicio=time(NULL);

	// Starting the mathematical model

	// Create the variables
	char varName[100];


	// --Create the variables of monetary cost
	for(int p=0; p < list.n; p++){
		for(int i=0; i < userRequest.nPackage; i++){
			for(int t=0; t < userRequest.timeMax; t++){
				// Write the variable
				sprintf(varName,"x_%d_%d_%d",p+1,i+1,t+1);

				// Add the variable
				UFFLP_AddVariable(prob, varName, 0, 1, list.package[p].price* userRequest.alpha1, UFFLP_Binary);
				//UFFLP_AddVariable(prob, varName, 0, 1, ((list.package[p].price*(userRequest.gflops/list.package[p].gflops)-min1)/(max1-min1))* userRequest.alpha1, UFFLP_Binary);


				//(((list.package[p].price-min)/(max-min))/(userRequest.timeMax*userRequest.nPackage))
				//(list.package[p].price/(userRequest.timeMax*userRequest.nPackage*max))
			}
		}
	}

	// --Create the variables of maximum time

	// Write the variable
	sprintf(varName,"tm");

	// Add the variable
	//UFFLP_AddVariable(prob, varName, 0, UFFLP_Infinity, userRequest.alpha2/userRequest.timeMax, UFFLP_Integer);
	UFFLP_AddVariable(prob, varName, 0, UFFLP_Infinity, userRequest.alpha2, UFFLP_Integer);

	// Create the constraints
	char consName[100];

	// --the constraint of monetary cost
	sprintf(consName,"Monetary_Cost");
	for(int p=0; p < list.n; p++){
		for(int i=0; i < userRequest.nPackage; i++){
			for(int t=0; t < userRequest.timeMax; t++){
				sprintf(varName,"x_%d_%d_%d",p+1,i+1,t+1);

				// Set the coefficient
				UFFLP_SetCoefficient(prob, consName, varName, list.package[p].price);
			}
		}
	}
	// Add the constraint
	UFFLP_AddConstraint(prob, consName, userRequest.cost, UFFLP_Less);

	// --the constraint of disk storage
	double M=999999;
	for(int pp=0; pp < list.n; pp++){
		for(int ii=0; ii < userRequest.nPackage; ii++){
			for(int t=0; t < userRequest.timeMax; t++){
				sprintf(consName,"Disk_Storage_%d_%d_%d", pp+1, ii+1, t+1);
				for(int p=0; p < list.n; p++){
					for(int i=0; i < userRequest.nPackage; i++){
						sprintf(varName,"x_%d_%d_%d",p+1,i+1, t+1);

						// Set the coefficient
						UFFLP_SetCoefficient(prob, consName, varName, list.package[p].disk);

						if(p==pp && i==ii)
							UFFLP_SetCoefficient(prob, consName, varName, list.package[p].disk-M);
					}
				}

				// Add the constraint
				UFFLP_AddConstraint(prob, consName, userRequest.disk - M, UFFLP_Greater);
			}
		}
	}

	// --the constraint of RAM memory
	for(int pp=0; pp < list.n; pp++){
		for(int ii=0; ii < userRequest.nPackage; ii++){
			for(int t=0; t < userRequest.timeMax; t++){
				sprintf(consName,"Memory_%d_%d_%d",pp+1, ii+1, t+1);
				for(int p=0; p < list.n; p++){
					for(int i=0; i < userRequest.nPackage; i++){
						sprintf(varName,"x_%d_%d_%d",p+1,i+1, t+1);

						// Set the coefficient
						UFFLP_SetCoefficient(prob, consName, varName, list.package[p].ram);

						if(p==pp && i==ii)
							UFFLP_SetCoefficient(prob, consName, varName, list.package[p].ram-M);
					}
				}

				// Add the constraint
				UFFLP_AddConstraint(prob, consName, userRequest.ram - M, UFFLP_Greater);
			}
		}
	}


	// --the constraint of Gflops
	sprintf(consName,"Gflops");
	for(int p=0; p < list.n; p++){
		for(int i=0; i < userRequest.nPackage; i++){
			for(int t=0; t < userRequest.timeMax; t++){
				sprintf(varName,"x_%d_%d_%d",p+1,i+1,t+1);

				// Set the coefficient
				UFFLP_SetCoefficient(prob, consName, varName, list.package[p].gflops);
			}
		}
	}
	// Add the constraint
	UFFLP_AddConstraint(prob, consName, userRequest.gflops, UFFLP_Greater);

	// --the constraints of the maximum number of package
	sprintf(consName,"Maximum_number_package");
	for(int t=0; t < userRequest.timeMax; t++){
		for(int p=0; p < list.n; p++){
			for(int i=0; i < userRequest.nPackage; i++){
				sprintf(varName,"x_%d_%d_%d",p+1,i+1,t+1);

				// Set the coefficient
				UFFLP_SetCoefficient(prob, consName, varName, 1);
			}
		}
		// Add the constraint
		UFFLP_AddConstraint(prob, consName, userRequest.nPackage, UFFLP_Less);
	}

	// --the constraints of the variable of maximum time

	for(int p=0; p < list.n; p++){
		for(int i=0; i < userRequest.nPackage; i++){
			for(int t=0; t < userRequest.timeMax; t++){

				sprintf(consName,"Maximum_time_variable_%d_%d_%d", p+1,i+1,t+1);

				sprintf(varName,"tm");
				// Set the coefficient
				UFFLP_SetCoefficient(prob, consName, varName, 1);

				sprintf(varName,"x_%d_%d_%d",p+1,i+1,t+1);
				// Set the coefficient
				UFFLP_SetCoefficient(prob, consName, varName, -(t+1));

				// Add the constraint
				UFFLP_AddConstraint(prob, consName, 0.0, UFFLP_Greater);
			}
		}
	}

	// --the constraints of if the package t is selected, the package t+1 is also selected

	for(int p=0; p < list.n; p++){
		for(int i=0; i < userRequest.nPackage; i++){
			for(int t=0; t < userRequest.timeMax-1; t++){

				sprintf(consName,"Selected_package_%d_%d_%d", p+1,i+1,t+1);

				sprintf(varName,"x_%d_%d_%d",p+1,i+1,(t+1)+1);
				// Set the coefficient
				UFFLP_SetCoefficient(prob, consName, varName, 1);

				sprintf(varName,"x_%d_%d_%d",p+1,i+1,t+1);
				// Set the coefficient
				UFFLP_SetCoefficient(prob, consName, varName, -1);

				// Add the constraint
				UFFLP_AddConstraint(prob, consName, 0.0, UFFLP_Less);
			}
		}
	}

	// --the constraints of ist packages are selected

	for(int p=0; p < list.n; p++){
		for(int i=0; i < userRequest.nPackage-1; i++){
			for(int t=0; t < userRequest.timeMax; t++){

				sprintf(consName,"ist_packages_%d_%d_%d", p+1,i+1,t+1);

				sprintf(varName,"x_%d_%d_%d",p+1,(i+1)+1,t+1);
				// Set the coefficient
				UFFLP_SetCoefficient(prob, consName, varName, 1);

				sprintf(varName,"x_%d_%d_%d",p+1,i+1,t+1);
				// Set the coefficient
				UFFLP_SetCoefficient(prob, consName, varName, -1);

				// Add the constraint
				UFFLP_AddConstraint(prob, consName, 0.0, UFFLP_Less);
			}
		}
	}

	// End of the mathematical model

	time_total = calculate_time(time_ini, clock());
	fim=time(NULL);
	cout << "The creation time of the MIP (clock) = " << time_total << endl;
	cout << "The creation time of the MIP = " << difftime(fim,inicio) << endl;

	// Write the problem to a file in the LP format
	if (writeLP){
		char file[100];
		strcpy(file,instanceFile.c_str());
		strcat(file,".lp");
		UFFLP_WriteLP(prob,file);
	}
	useHeuristicSolution=1;
	if(useHeuristicSolution)
		setVariables(prob, list, userRequest, solution);

	// Configure both the log file and the log level
	if (writeLog){
		char file[100];
		strcpy(file,instanceFile.c_str());
		strcat(file,".log");
		UFFLP_SetLogInfo(prob, file, 2);
	}
	else
		UFFLP_SetLogInfo(prob, "", 0);


	// Configure the execution time in seconds = 24 hours -> 86400 seconds
	UFFLP_SetParameter(prob, UFFLP_TimeLimit,86400);

	// Configure the threads number of CPLEX
	//UFFLP_SetCplexParameter(prob, 1067, UFFLP_IntegerParam, 1);


	time_ini = (unsigned long int) clock();
	inicio=time(NULL);



	// Solve the problem
	UFFLP_StatusType status = UFFLP_Solve(prob, UFFLP_Minimize);
	UFFLP_StatusType status_ini = status;

	if(useHeuristicSolution){
		notSetVariables(prob, list, userRequest, solution);
		status = UFFLP_Solve(prob, UFFLP_Minimize);
	}

	time_total = calculate_time(time_ini, clock());
	fim=time(NULL);
	cout << "The resolution time of the MIP (clock) = " << time_total << endl;
	cout << "The resolution time of the MIP = " << difftime(fim,inicio) << endl;

	if ((status == UFFLP_Feasible) || (status == UFFLP_Optimal)){

		// Show a message
		if(status_ini == UFFLP_Optimal)
			cout<< "Optimal solution found." << endl;
		else
			cout<< "Feasible solution found." << endl;

		// Get the solution value
		double value;
		UFFLP_GetObjValue(prob, &value);

		// Write the solution value
		cout<< "The objetive function value = " << value << endl;

		sprintf(varName, "tm");
		double value2 = 0.0;
		UFFLP_GetSolution(prob, varName, &value2);

		//cout << "Cost= " << (value - (alpha2*value2/userRequest.timeMax))/alpha1 << endl;

		//cout << "Time= " << value2/userRequest.timeMax << endl;
		int nPack[list.n], nTime[list.n];
		double cost=0.0;

		//Get the solution of the monetary cost variables
		for(int p=0; p < list.n; p++){
			int major=-1;
			nTime[p]=0;
			for(int t=0; t < userRequest.timeMax; t++){
				nPack[p]=0;
				for(int i=0; i < userRequest.nPackage; i++){
					sprintf(varName, "x_%d_%d_%d", p+1, i+1,t+1);
					value = 0.0;
					UFFLP_GetSolution(prob, varName, &value);
					if (value > 0.5){
						nPack[p]++;
						nTime[p]++;
						cost+= list.package[p].price;
					}

					//cout << "x_" << p+1 << "_" << i+1 << "_" << t+1 << " = " << value << endl;
				}
				if(nPack[p]>major)
					major=nPack[p];

			}
			nPack[p]=major;
		}

		//Write the solution
		for(int p=0; p < list.n; p++){
			cout << "Package " << list.package[p].id << " = " << nPack[p] << " (" << nTime[p] << " units)" << endl;
		}

		//Get the solution of maximum time variables
		sprintf(varName, "tm");
		value = 0.0;
		UFFLP_GetSolution(prob, varName, &value);
		cout << "Maximum time = " << value << " hours" << endl;

		cout << "Monetary Cost = $" << cost << endl;


	}
	else{
		if(status == UFFLP_Infeasible)
			// Show a message
			cout << "Infeasible problem." << endl;
		 else
			// Show a message
			cout << "Generic error in optimazation." << endl;
	}

	// Destroy the problem
	UFFLP_DestroyProblem (prob);
	return 0;
}*/
bool compare_cost(Tpackage const& p1, Tpackage const& p2){
	return p1.price < p2.price;
}

bool compare_gflops(Tpackage const& p1, Tpackage const& p2){
	return p1.gflops > p2.gflops;
}

bool compare_fo(Tpackage const& p1, Tpackage const& p2){
	return p1.fo < p2.fo;
}

void heuristic(TpackageList* list, TuserRequest userRequest, double alpha1, double alpha2, int np, int* tm, int it, int** solution){
    int package_id, num_time;
    double total_cost=0.0, total_mem=0.0, total_disk=0.0, total_gflops=0.0;
    int selected_package[list->n][2*userRequest.timeMax];
    np=list->n;
    num_selected_package=0;
    enter=0;

	for(int p=0; p < list->n; p++){
		for(int t=0; t < 2*userRequest.timeMax; t++){
			selected_package[p][t]=0;
		}
		for(int i=0; i < userRequest.nPackage; i++){
			solution[p][i]=0;
		}
	}

	for(int p=0; p < 2*userRequest.timeMax; p++){
		t_ram[p]=0.0;
		t_hd[p]=0.0;

	}

    //Set the first time
    num_time=1;

    while ((num_selected_package < userRequest.nPackage) || (total_gflops < userRequest.gflops)){
		//Randomly select a package from the n-th ordered list.
		package_id=rand()%np;

		// Update the resources
		while (((total_mem < userRequest.ram) || (total_disk < userRequest.disk)) && (total_cost <= userRequest.cost) && (num_selected_package + 1<= userRequest.nPackage)){
			int aux=package_id;
			if(num_selected_package + 1<= userRequest.nPackage){
				if((userRequest.ram-total_mem)/(userRequest.nPackage-num_selected_package) > list->package[package_id].ram){
					//package_id=pack;
					package_id=pack_ram[rand()%pr];

				}

				if(num_time-1 == 0 || (num_time-1>0 && selected_package[list->package[package_id].id][num_time-2])==0)
				num_selected_package++;
				selected_package[list->package[package_id].id][num_time-1]++;
				total_cost += list->package[package_id].price;
				total_mem += list->package[package_id].ram;
				total_disk += list->package[package_id].disk;
				total_gflops += list->package[package_id].gflops;
				t_ram[num_time]+= list->package[package_id].ram;
				t_hd[num_time]+= list->package[package_id].disk;
				package_id=aux;

			}

			if(num_selected_package + 1<= userRequest.nPackage){
				if((userRequest.disk-total_disk)/(userRequest.nPackage-num_selected_package) > list->package[package_id].disk){
					//package_id=pack_hd;
					package_id=pack_disk[rand()%ph];
					if(num_time-1 == 0 || (num_time-1>0 && selected_package[list->package[package_id].id][num_time-2])==0)
					num_selected_package++;
					selected_package[list->package[package_id].id][num_time-1]++;
					total_cost += list->package[package_id].price;
					total_mem += list->package[package_id].ram;
					total_disk += list->package[package_id].disk;
					total_gflops += list->package[package_id].gflops;
					t_ram[num_time]+= list->package[package_id].ram;
					t_hd[num_time]+= list->package[package_id].disk;
					package_id=aux;

				}

			}
		}

		if(total_cost > userRequest.cost){
			num_time++;
			if(DEBUG)
			cout<< "sai1" << endl;
			break;
		}

		if ((num_time==1) && (total_mem > userRequest.ram)){
			if(DEBUG)
			cout << " Memory satisfied." << endl;
		}
		if ((num_time==1) && (total_disk > userRequest.disk)){
			if(DEBUG)
				cout << " Disk capacity satisfied." << endl;
		}

		while ((total_gflops < userRequest.gflops) && (num_selected_package + 1<= userRequest.nPackage) && (total_cost <= userRequest.cost)){
			if(num_time-1 == 0 || (num_time-1>0 && selected_package[list->package[package_id].id][num_time-2])==0)
			num_selected_package++;
			selected_package[list->package[package_id].id][num_time-1]++;
			total_cost += list->package[package_id].price;
			total_mem += list->package[package_id].ram;
			total_disk += list->package[package_id].disk;
			total_gflops += list->package[package_id].gflops;
			t_ram[num_time]+= list->package[package_id].ram;
			t_hd[num_time]+= list->package[package_id].disk;
			
		}

		if(total_cost > userRequest.cost){
			num_time++;
			if(DEBUG)
			cout<< "sai2" << endl;
			break;
		}

		if(total_gflops >= userRequest.gflops){

			num_time++;
			if(DEBUG)
			cout<< "sai3" << endl;
			break;
		}
		else{
			double mem=0.0, disk=0.0;
			for(int p=0; p < list->n; p++){
				if((selected_package[list->package[p].id][num_time-1]>0)){

					for(int np=0; np < selected_package[list->package[p].id][num_time-1]; np++){
						selected_package[list->package[p].id][num_time]++;
						total_gflops += list->package[p].gflops;
						total_cost += list->package[p].price;
						t_ram[num_time+1]+= list->package[p].ram;
						t_hd[num_time+1]+= list->package[p].disk;
						mem+=list->package[p].ram;
						disk+=list->package[p].disk;
						if(mem>= userRequest.ram && disk>= userRequest.disk && total_gflops >= userRequest.gflops)
							break;
					}
					
				}
				if(mem>= userRequest.ram && disk>= userRequest.disk && total_gflops >= userRequest.gflops)
					break;

			}

			if(total_gflops > userRequest.gflops){
				num_time+=2;
				if(DEBUG)
				cout<< "sai4" << endl;
				break;
			}
			num_time++;
		
			package_id++;
			if(package_id >= list->n)
				package_id=0;
		}
	}
	if(total_cost > userRequest.cost){
		if(DEBUG) cout << "Cost exceeded." << endl;
	}

	if(num_time> userRequest.timeMax){
		if(DEBUG) cout << "Time exceeded." << endl;
	}

	if(num_selected_package > userRequest.nPackage){
		if(DEBUG) cout << "Package number exceeded." << endl;
	}
	for(int p=0; p < list->n; p++){
		for(int t=0; t < 2*userRequest.timeMax; t++){
			for(int i=0; i < selected_package[list->package[p].id][t]; i++){
				solution[list->package[p].id][i]++;
			}
		}

	}
	if(DEBUG){
		for(int p=0; p < list->n; p++){
			cout << "Package [" << p << "]" << endl;
			for(int i=0; i < userRequest.nPackage; i++){
				if (solution[p][i]!=0){
					cout << "["<< i << "] = " << solution[p][i] << endl;
				}
			}

		}
	}
	if(DEBUG){
	    cout << "[Heu] Maximum time = " << num_time-1 << " hours" << endl;
		cout << "[Heu] Monetary Cost = $" << total_cost << endl;
	}
	
    (*tm)=num_time-1;
    total_ram=total_mem;
    total_hd=total_disk;
    total_flops=total_gflops;
}

double max(double a1, double a2){
	if (a1 > a2)
		return a1;
	else return a2;
}

double max_3(double a1, double a2, double a3){
	if (a1 > a2){
		if(a1 > a3)
			return a1;
	}
	else if (a2> a3)
		return a2;
	return a3;

}

double calculate_cost_function(TpackageList* list, TuserRequest userRequest, double alpha1, double alpha2, int **solution, int tm){
	double cp=0.0, f=0.0;
	for(int p=0; p < list->n; p++){
		for(int i=0; i < userRequest.nPackage; i++){
			if (solution[list->package[p].id][i]!=0)
			  cp+=solution[list->package[p].id][i]*list->package[p].price;
		}
	}
	if(DEBUG) cout << "c(p)= " << cp << endl;
	f = MX*max(0, tm-userRequest.timeMax) + MX*max(0,cp-userRequest.cost)+ alpha1*cp + alpha2*tm;
	return f;
}

int improved(double fant, double fnew){
	if((fnew < fant) || (enter==1))
		return 1;
	else return 0;
}

int swap_1(TpackageList* list, TuserRequest userRequest,double alpha1, double alpha2, int **solution, int* tm, double* fant, double * fnew){
	for(int p=0; p < list->n; p++){
		for(int i=0; i < userRequest.nPackage; i++){
			if(solution[list->package[p].id][i]!=0){
				for(int tp=0; tp < list->n; tp++){
					if(list->package[p].id != list->package[tp].id){ // if the packages are different, attempt to change the package p for tp
						int max1=-1, max2=-1, major=-1;
						for(int pp=0; pp < list->n; pp++){
							for(int pi=0; pi < userRequest.nPackage; pi++){
								if((solution[list->package[pp].id][pi]==*tm) && (pp!=p || pi !=i)) {
									max1=*tm;
									break;
								}
								if(solution[list->package[pp].id][pi]> max2 && solution[list->package[pp].id][pi]< *tm)
									max2=solution[list->package[pp].id][pi];
							}
						}
						if(max1>0) major=max1;
						else major=max2;

						int taux= solution[list->package[p].id][i], tnew;
						double tram= list->package[p].ram, thd= list->package[p].disk, tglops=list->package[p].gflops*taux;
						if((total_flops-tglops)>=userRequest.gflops){
							tnew=1;
						}
						else tnew=ceil((userRequest.gflops-(total_flops-tglops))/list->package[tp].gflops);

						int ma=0, satisfy=1;

						if(solution[list->package[p].id][i]> ma)
							ma=solution[list->package[p].id][i];
						if(tnew> ma)
							ma=tnew;

						for(int mi=1; mi<=ma; mi++){
							double aux_mem=t_ram[mi], aux_hd=t_hd[mi];
							if(mi<=solution[list->package[p].id][i]){
								aux_mem-=list->package[p].ram;
								aux_hd-=list->package[p].disk;
							}
							if(mi<=tnew){
								aux_mem+=list->package[tp].ram;
								aux_hd+=list->package[tp].disk;
							}
							if(aux_mem<userRequest.ram || aux_hd < userRequest.disk){
								if(mi>tnew){
									if(aux_mem<userRequest.ram && aux_mem+list->package[tp].ram>=userRequest.ram){
										tnew=mi;
										aux_mem+=list->package[tp].ram;
										aux_hd+=list->package[tp].disk;
									}
									else if(aux_hd<userRequest.disk && aux_hd+list->package[tp].disk>=userRequest.disk){
										tnew=mi;
										aux_mem+=list->package[tp].ram;
										aux_hd+=list->package[tp].disk;
									}
								}

								if(aux_mem<userRequest.ram|| aux_hd < userRequest.disk){
									satisfy=0;
									break;
								}
							}

						}

						if(satisfy){
							if((tnew<= userRequest.timeMax)&& ((alpha1*tnew*list->package[tp].price+alpha2*max(major, tnew))-(alpha1*taux*list->package[p].price + alpha2*(*tm))<-0.000001)){ // verify if maximum time is satisfied and the cost improved
								// Change the package p in the solution
								solution[list->package[p].id][i]=0;
								for(int ii=0; ii < userRequest.nPackage; ii++){
									if((solution[list->package[tp].id][ii])==0){
										solution[list->package[tp].id][ii]=tnew;
										break;
									}
								}
								*fant=*fnew;
								*fnew=*fant-alpha1*taux*list->package[p].price+alpha1*tnew*list->package[tp].price -alpha2*(*tm)+ alpha2*(max(major, tnew))-  MX*max(0, *tm-userRequest.timeMax);
								total_flops= total_flops - tglops + tnew*list->package[tp].gflops;
								total_ram= total_ram - tram + list->package[tp].ram;
								total_hd = total_hd- thd + list->package[tp].disk;

								if(taux> ma)
									ma=taux;
								if(tnew> ma)
									ma=tnew;

								for(int mi=1; mi<=ma; mi++){
									double aux_mem=t_ram[mi], aux_hd=t_hd[mi];
									if(mi<=taux){
										aux_mem-=list->package[p].ram;
										aux_hd-=list->package[p].disk;
									}
									if(mi<=tnew){
										aux_mem+=list->package[tp].ram;
										aux_hd+=list->package[tp].disk;
									}
									t_ram[mi]=aux_mem;
									t_hd[mi]=aux_hd;

								}


								if(tnew>major)
									*tm=tnew;
								else
									*tm=major;
								 *fnew+=MX*max(0, *tm-userRequest.timeMax);

								if(tnew==0)
									num_selected_package--;


								
								if(DEBUG){
									cout << "Swap! fant= " << *fant << " fnew= " << *fnew << " tm= " << *tm  << " cp = " << (*fnew- (alpha2*(*tm)))/ alpha1<< endl;
									cout << "pacote= " << list->package[p].id << " substituto= " << list->package[tp].id << " tnew= " << tnew << endl;
									cout << "total gflop = " << total_flops << endl;
									
								}
								if(*fant<0) exit(1);
								return 1;
							}
						}
					}
				}
			}
			
		}
	}
	if(DEBUG) cout << "No Swap!" << endl;

	return 0;
}

int swap_2(TpackageList* list, TuserRequest userRequest,double alpha1, double alpha2, int **solution, int* tm, double* fant, double * fnew){
	for(int p=0; p < list->n; p++){
		for(int i=0; i < userRequest.nPackage; i++){
			if(solution[list->package[p].id][i]!=0){ //package 1
				for(int tp=0; tp < list->n; tp++){
					for(int ti=0; ti < userRequest.nPackage; ti++){
						if(((list->package[p].id!=list->package[tp].id)||((list->package[p].id==list->package[tp].id)&&(i!=ti)))){ //package 2
							int max1=-1, max2=-1, major=-1;
							for(int pp=0; pp < list->n; pp++){
								for(int pi=0; pi < userRequest.nPackage; pi++){
									if((solution[list->package[pp].id][pi]==*tm) && (pp!=p || pi !=i) && (pp !=tp || pi!=ti)) {
										max1=*tm;
										break;
									}
									if(solution[list->package[pp].id][pi]> max2 && solution[list->package[pp].id][pi]< *tm)
										max2=solution[list->package[pp].id][pi];
								}
							}
							if(max1>0) major=max1;
							else major=max2;

							for(int p1=0; p1 < list->n; p1++){
								for(int p2=0; p2 < list->n; p2++){
									int tp1=0, tp2=0, tpp=0;
									if (solution[list->package[tp].id][ti]) tpp=1;
										int tnew;
										if((total_flops-(solution[list->package[p].id][i]*list->package[p].gflops + solution[list->package[tp].id][ti]*list->package[tp].gflops))>=userRequest.gflops)
											tnew=1;
										else tnew= ceil((double)(userRequest.gflops-(total_flops-(solution[list->package[p].id][i]*list->package[p].gflops + solution[list->package[tp].id][ti]*list->package[tp].gflops)))/(list->package[p1].gflops+list->package[p2].gflops));

										int t1=tnew,t2=tnew, cont=0;

										int satisfy=0;
										if(cont==0){
												int satisfy1=1;
												int ttp1=-1, ttp2=-1, tt1, tt2;
												double best_cost=999999;

												for(t1=0; t1< tnew ;  t1++){
													t2=ceil((double)(userRequest.gflops-(total_flops-
															(solution[list->package[p].id][i]*list->package[p].gflops + solution[list->package[tp].id][ti]*list->package[tp].gflops)
															+t1*list->package[p1].gflops))/list->package[p2].gflops);
													if(t2<0) t2=1;
													tt1=t1; tt2=t2;

													int ma=0;
													satisfy1=1;

													
													if(solution[list->package[p].id][i]> ma)
														ma=solution[list->package[p].id][i];
													if(solution[list->package[tp].id][ti]> ma)
														ma=solution[list->package[tp].id][ti];
													if(t1>ma)
														ma=t1;
													if(t2>ma)
														ma=t2;

													for(int mi=1; mi<=ma; mi++){
														double aux_mem=t_ram[mi], aux_hd=t_hd[mi];
														if(mi<=solution[list->package[p].id][i]){
															aux_mem-=list->package[p].ram;
															aux_hd-=list->package[p].disk;
														}
														if(mi<=solution[list->package[tp].id][ti]){
															aux_mem-=list->package[tp].ram;
															aux_hd-=list->package[tp].disk;
														}
														if(mi<=t1){
															aux_mem+=list->package[p1].ram;
															aux_hd+=list->package[p1].disk;
														}
														if(mi<=t2){
															aux_mem+=list->package[p2].ram;
															aux_hd+=list->package[p2].disk;
														}
														if(aux_mem<userRequest.ram || aux_hd < userRequest.disk){
															if(mi>t1){
																if(aux_mem<userRequest.ram && aux_mem+list->package[p1].ram>=userRequest.ram){
																	t1=mi;
																	aux_mem+=list->package[p1].ram;
																	aux_hd+=list->package[p1].disk;
																}
																else if(aux_hd<userRequest.disk && aux_hd+list->package[p1].disk>=userRequest.disk){
																	t1=mi;
																	aux_mem+=list->package[p1].ram;
																	aux_hd+=list->package[p1].disk;
																}
															}
															if(mi>t2){
																if(aux_mem<userRequest.ram && aux_mem+list->package[p2].ram>=userRequest.ram){
																	t2=mi;
																	aux_mem+=list->package[p2].ram;
																	aux_hd+=list->package[p2].disk;
																}
																else if(aux_hd<userRequest.disk && aux_hd+list->package[p2].disk>=userRequest.disk){
																	t2=mi;
																	aux_mem+=list->package[p2].ram;
																	aux_hd+=list->package[p2].disk;
																}
															}
															if(aux_mem<userRequest.ram|| aux_hd < userRequest.disk){
																satisfy1=0;
																break;
															}
														}

													}
													tp1=0; tp2=0;
													if (t1) tp1=1;
													if (t2) tp2=1;

													
													if( (satisfy1)
														&&(t1*list->package[p1].gflops + t2*list->package[p2].gflops >=
															(userRequest.gflops-(total_flops-(solution[list->package[p].id][i]*list->package[p].gflops + solution[list->package[tp].id][ti]*list->package[tp].gflops))))
															&&((alpha1*(t1*list->package[p1].price + t2* list->package[p2].price) + alpha2* max_3(major, t1, t2))- (alpha1*(solution[list->package[p].id][i]*list->package[p].price + solution[list->package[tp].id][ti]*list->package[tp].price) + alpha2*(*tm))<-0.000001)
																&&((alpha1*(t1*list->package[p1].price + t2* list->package[p2].price) + alpha2* max_3(major, t1, t2))- best_cost < -0.000001)){
																	ttp1=t1;
																	ttp2=t2;

																	 best_cost=(alpha1*(t1*list->package[p1].price + t2* list->package[p2].price) + alpha2* max_3(major, t1, t2))- (alpha1*(solution[list->package[p].id][i]*list->package[p].price + solution[list->package[tp].id][ti]*list->package[tp].price) + alpha2*(*tm));
																	break;
														
													}
													t1=tt1;
													t2=tt2;

												}
												int satisfy2=1;
												for(t2=0; t2< tnew ;  t2++){
													t1=ceil((double)(userRequest.gflops-(total_flops-
															(solution[list->package[p].id][i]*list->package[p].gflops + solution[list->package[tp].id][ti]*list->package[tp].gflops)
															+t2*list->package[p2].gflops))/list->package[p1].gflops);
													if(t1<0) t1=1;

													tt1=t1; tt2=t2;

													int ma=0;
													satisfy2=1;

													

													if(solution[list->package[p].id][i]> ma)
														ma=solution[list->package[p].id][i];
													if(solution[list->package[tp].id][ti]> ma)
														ma=solution[list->package[tp].id][ti];
													if(t1>ma)
														ma=t1;
													if(t2>ma)
														ma=t2;

													for(int mi=1; mi<=ma; mi++){
														double aux_mem=t_ram[mi], aux_hd=t_hd[mi];
														if(mi<=solution[list->package[p].id][i]){
															aux_mem-=list->package[p].ram;
															aux_hd-=list->package[p].disk;
														}
														if(mi<=solution[list->package[tp].id][ti]){
															aux_mem-=list->package[tp].ram;
															aux_hd-=list->package[tp].disk;
														}
														if(mi<=t1){
															aux_mem+=list->package[p1].ram;
															aux_hd+=list->package[p1].disk;
														}
														if(mi<=t2){
															aux_mem+=list->package[p2].ram;
															aux_hd+=list->package[p2].disk;
														}
														if(aux_mem<userRequest.ram || aux_hd < userRequest.disk){
															if(mi>t1){
																if(aux_mem<userRequest.ram && aux_mem+list->package[p1].ram>=userRequest.ram){
																	t1=mi;
																	aux_mem+=list->package[p1].ram;
																	aux_hd+=list->package[p1].disk;
																}
																else if(aux_hd<userRequest.disk && aux_hd+list->package[p1].disk>=userRequest.disk){
																	t1=mi;
																	aux_mem+=list->package[p1].ram;
																	aux_hd+=list->package[p1].disk;
																}
															}
															if(mi>t2){
																if(aux_mem<userRequest.ram && aux_mem+list->package[p2].ram>=userRequest.ram){
																	t2=mi;
																	aux_mem+=list->package[p2].ram;
																	aux_hd+=list->package[p2].disk;
																}
																else if(aux_hd<userRequest.disk && aux_hd+list->package[p2].disk>=userRequest.disk){
																	t2=mi;
																	aux_mem+=list->package[p2].ram;
																	aux_hd+=list->package[p2].disk;
																}
															}
															if(aux_mem<userRequest.ram|| aux_hd < userRequest.disk){
																satisfy2=0;
																break;
															}
														}

													}
													tp1=0; tp2=0;
													if (t1) tp1=1;
													if (t2) tp2=1;
													
													if( (satisfy2)
														&&(t1*list->package[p1].gflops + t2*list->package[p2].gflops >=(userRequest.gflops-(total_flops-(solution[list->package[p].id][i]*list->package[p].gflops + solution[list->package[tp].id][ti]*list->package[tp].gflops))))
															&&((alpha1*(t1*list->package[p1].price + t2* list->package[p2].price) + alpha2* max_3(major, t1, t2))- (alpha1*(solution[list->package[p].id][i]*list->package[p].price + solution[list->package[tp].id][ti]*list->package[tp].price) + alpha2*(*tm))<-0.000001)
																&&(((alpha1*(t1*list->package[p1].price + t2* list->package[p2].price) + alpha2* max_3(major, t1, t2)))- best_cost < -0.000001)){
																	ttp1=t1;
																	ttp2=t2;

																	 best_cost=((alpha1*(t1*list->package[p1].price + t2* list->package[p2].price) + alpha2* max_3(major, t1, t2))- (alpha1*(solution[list->package[p].id][i]*list->package[p].price + solution[list->package[tp].id][ti]*list->package[tp].price) + alpha2*(*tm)));
																	break;
														
													}
													t1=tt1;
													t2=tt2;

												}
												tp1=0; tp2=0;

												if(ttp1!=-1 && ttp2!=-1){
													t1=ttp1;
													t2=ttp2;
													satisfy=1;
												}else{
													t1=tnew;
													t2=tnew;
												}
											}

											if (t1) tp1=1;
											if (t2) tp2=1;



											if(satisfy)
											if((solution[list->package[tp].id][ti]!=0) || ((solution[list->package[tp].id][ti]==0) && (t1>0 && t2>0) && (num_selected_package +1 <= userRequest.nPackage))
												|| ((solution[list->package[tp].id][ti]==0) && (t1==0 || t2==0) )){

												
												if (((alpha1*(t1*list->package[p1].price + t2* list->package[p2].price) + alpha2* max_3(major, t1, t2))- (alpha1*(solution[list->package[p].id][i]*list->package[p].price + solution[list->package[tp].id][ti]*list->package[tp].price) + alpha2*(*tm))<-0.000001)&&
												  
												   (t1*list->package[p1].gflops + t2* list->package[p2].gflops)>=(userRequest.gflops-(total_flops-(solution[list->package[p].id][i]*list->package[p].gflops + solution[list->package[tp].id][ti]*list->package[tp].gflops)))){


													// Change the package p in the solution
													int taux1=solution[list->package[p].id][i], taux2=solution[list->package[tp].id][ti];
													solution[list->package[p].id][i]=0;
													solution[list->package[tp].id][ti]=0;
													for(int ii=0; ii < userRequest.nPackage; ii++){
														if((solution[list->package[p1].id][ii])==0){
															solution[list->package[p1].id][ii]=t1;
															break;
														}
													}
													for(int ii=0; ii < userRequest.nPackage; ii++){
														if((solution[list->package[p2].id][ii])==0){
															solution[list->package[p2].id][ii]=t2;
															break;
														}
													}
													



													*fant=*fnew;

													*fnew=*fant - alpha1*(taux1*list->package[p].price + taux2*list->package[tp].price)
																+ alpha1*(t1*list->package[p1].price+ t2*list->package[p2].price)
																- alpha2*(*tm) + alpha2*(max_3(major, t1, t2))- MX*max(0, *tm-userRequest.timeMax);
													total_flops= total_flops - (taux1*list->package[p].gflops + taux2*list->package[tp].gflops) + (t1*list->package[p1].gflops)+(t2*list->package[p2].gflops);

													total_ram= total_ram - (list->package[p].ram+tpp*list->package[tp].ram) + (tp1*list->package[p1].ram+tp2*list->package[p2].ram);
													total_hd = total_hd - (list->package[p].disk+tpp*list->package[tp].disk) + (tp1*list->package[p1].disk+tp2*list->package[p2].disk);

													double ma=0.0;
													if(taux1> ma)
														ma=taux1;
													if(taux2> ma)
														ma=taux2;
													if(t1>ma)
														ma=t1;
													if(t2>ma)
														ma=t2;

													for(int mi=1; mi<=ma; mi++){
														double aux_mem=t_ram[mi], aux_hd=t_hd[mi];
														if(mi<=taux1){
															aux_mem-=list->package[p].ram;
															aux_hd-=list->package[p].disk;
														}
														if(mi<=taux2){
															aux_mem-=list->package[tp].ram;
															aux_hd-=list->package[tp].disk;
														}
														if(mi<=t1){
															aux_mem+=list->package[p1].ram;
															aux_hd+=list->package[p1].disk;
														}
														if(mi<=t2){
															aux_mem+=list->package[p2].ram;
															aux_hd+=list->package[p2].disk;
														}
														t_ram[mi]=aux_mem;
														t_hd[mi]=aux_hd;

													}
													


													if(t1>=major && t1>=t2)
														*tm=t1;
													if(t2>=major && t2>=t1)
														*tm=t2;
													if (major >= t1 && major >= t2)
													   *tm=major;

													*fnew+=MX*max(0, *tm-userRequest.timeMax);

													if (taux2==0 && t1>0 && t2>0)
															num_selected_package++;
													else if (taux2==0 && t1==0 && t2==0)
														num_selected_package--;

													else if (taux2>0 && t1>0 && t2==0)
														num_selected_package--;
													else if (taux2>0 && t1==0 && t2>0)
														num_selected_package--;
													else if (taux2>0 && t1==0 && t2==0)
														num_selected_package-=2;

													//cout<< "swap 2"<< endl;
													if(total_ram< userRequest.ram){
														cout <<" error!! " << endl;
														cout << "pack[" << list->package[p].id << "][" << solution[list->package[p].id][i] << "] ->"
															 << "pack[" << list->package[p1].id << "][" << t1 << "]" << endl;
														cout << "pack[" << list->package[tp].id << "][" << solution[list->package[tp].id][ti] << "] ->"
															 << "pack[" << list->package[p2].id << "][" << t2 << "]" << endl;
													}
													//cout << "swap 2 - 2" << endl;
													if(DEBUG){
														
														cout << "pacotes= " << list->package[p].id << ", " << list->package[tp].id <<  " substitutos= " << list->package[p1].id << ", " << list->package[p2].id << " t1= " << t1 << " t2= "<< t2 << endl;
														cout << " parc1 = " << (taux1*list->package[p].price + taux2*list->package[tp].price) << " parc2 = " << t1*list->package[p1].price+ t2*list->package[p2].price  << " tempos = " << taux1 << " , " << taux2<< endl;
														cout << "total gflop = " << total_flops << endl;
														cout << " gflop_ant = " << (taux1*list->package[p].gflops + taux2*list->package[tp].gflops) << endl;
														cout << "gflop_new = " << (t1*list->package[p1].gflops)+(t2*list->package[p2].gflops);
														cout << " f atual = " << (alpha1*((t1)*list->package[p1].price + (t2)* list->package[p2].price) + alpha2* max_3(*tm, t1, t2)) << " f ant = "<< (alpha1*(solution[list->package[p].id][i]*list->package[p].price + solution[list->package[tp].id][ti]*list->package[tp].price) + alpha2*(*tm)) << endl;
														//getchar();
													}


													if(*fant<0) exit(1);
													
													return 1;

												}

											}
										}
									}
								}
					}
				}
			}
		}
	}
	return 0;
}

void local_search(TpackageList* list, TuserRequest userRequest, double alpha1, double alpha2, int **solution, int* tm,  double* fnew, double * fant){

	if(DEBUG) cout << "f= " << *fnew << endl;
    int tt=0;

    //unsigned long int time_ini = (unsigned long int) clock();
    while (improved(*fant, *fnew)){
    	*fant=*fnew;
    	enter=0;
    	tt++;

    	// attempt to change one package
    	if (!swap_1(list,userRequest, alpha1, alpha2, solution,tm,fant,fnew)){ // if not improved the cost function
    		if(DEBUG){
    		cout << "swap1" << endl;
    		for(int p=0; p < list->n; p++){
				//if(DEBUG)
					cout << "Package [" << p << "]" << endl;
				for(int i=0; i < userRequest.nPackage; i++){
					if (solution[p][i]!=0){
						//if(DEBUG)
							cout << "["<< i << "] = " << solution[p][i] << endl;
					}
				}
			}
    		cout << "package = " << num_selected_package << endl;
    		}

    		
		// attempt to change two packages
		if(!swap_2(list,userRequest, alpha1, alpha2, solution,tm,fant,fnew)){
			 
		}
		if(DEBUG){
			//cout<< "swap2" << endl;
			for(int p=0; p < list->n; p++){
				//if(DEBUG)
				cout << "Package [" << p << "]" << endl;
				for(int i=0; i < userRequest.nPackage; i++){
					if (solution[p][i]!=0){
					   //if(DEBUG)
					    cout << "["<< i << "] = " << solution[p][i] << endl;
					}
				}
			}
			cout << "package = " << num_selected_package << endl;
		}
    	}
    	if(DEBUG) cout << "t = " << tt << endl;
    }
    double cost_t=0.0;
    if(DEBUG)
    for(int p=0; p < list->n; p++){
    		cout << "Package [" << p << "]" << endl;
		for(int i=0; i < userRequest.nPackage; i++){
			if (solution[p][i]!=0){
					cout << "["<< i << "] = " << solution[p][i] << endl;
			}
			cost_t+=solution[list->package[p].id][i]*list->package[p].price;
		}
	}
    if (DEBUG){
		cout << "[LS] Maximum time = " << *tm << " hours" << endl;
		cout << "[LS] Monetary Cost = $" << cost_t << endl;
    }
    if(total_ram< userRequest.ram){
	   cout <<" error!! " << endl;
    }
    //cout << "esperando.." << endl;
    ////getchar();
}

void copy_solution(TpackageList list, TuserRequest userRequest,int nl, int np, int** solution_best, int** solution){
	total_ram=0;
	total_hd=0;
	total_flops=0;
	num_selected_package=0;
	//double cost=0;
	for(int p=0; p < nl; p++){
		for(int i=0; i < np; i++){
			solution_best[list.package[p].id][i]=solution[list.package[p].id][i];
			//cost+=solution[list.package[p].id][i]*list.package[p].price;
			if(solution[list.package[p].id][i]>0){
				total_ram+=list.package[p].ram;
				total_hd+=list.package[p].disk;
			}
			total_flops+= solution[list.package[p].id][i]* list.package[p].gflops;
			if(solution[list.package[p].id][i]>0)
			num_selected_package++;
		}
	}

}

/*void setVariables(UFFProblem *prob, TpackageList list, TuserRequest userRequest, int** solution){
	char varName[100];
	int major=-1;
	for(int p=0; p < list.n; p++){
		for(int i=0; i < userRequest.nPackage; i++){
			for(int t=0; t< solution[list.package[p].id][i]; t++){
				sprintf(varName, "x_%d_%d_%d", p+1, i+1,t+1);
				UFFLP_ChangeBounds(prob, varName, 1, 1);
			}
			//for(int t=solution[list.package[p].id][i]; t< userRequest.timeMax; t++){
			//	sprintf(varName, "x_%d_%d_%d", p+1, i+1,t+1);
			//	UFFLP_ChangeBounds(prob, varName, 0, 0);
			//}
			if(solution[list.package[p].id][i]> major)
				major=solution[list.package[p].id][i];
		}
	}
	sprintf(varName, "tm");
	//UFFLP_ChangeBounds(prob, varName, major, major);
}

void notSetVariables(UFFProblem *prob, TpackageList list, TuserRequest userRequest, int** solution){
	char varName[100];
	for(int p=0; p < list.n; p++){
		for(int i=0; i < userRequest.nPackage; i++){
			for(int t=0; t< solution[list.package[p].id][i]; t++){
				sprintf(varName, "x_%d_%d_%d", p+1, i+1,t+1);
				UFFLP_ChangeBounds(prob, varName, 0, 1);
			}
		}
	}
	//sprintf(varName, "tm");
	//UFFLP_ChangeBounds(prob, varName, 0, UFFLP_Infinity);
}*/

