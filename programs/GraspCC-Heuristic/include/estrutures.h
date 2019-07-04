#ifndef _ESTRUTURAS_H
#define _ESTRUTURAS_H

#include <iostream>
#include <vector>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <fstream>
#include <string.h>
#include <math.h>
#include <time.h>
#include <algorithm>

/*#ifdef __LINUX__
#include "UFFLP.h"
#else
#include "../UFFLP/UFFLP.h"
#endif*/

using namespace std;

#define _64BITS 1
#define _32BITS 2

#define MX 1000
#define DEBUG 0

#ifdef _EM_ESTRUTURAS
#define EXTERN
#else
#define EXTERN extern
#endif

EXTERN double price_max;
/**********************************************STRUCTURES**************************************************/

/***************PACKAGE***************/
struct Tpackage{
	int id;
	double gflops;
	double ram;
	double disk;
	int plataform;
    double price;
    double fo;
};

/************PACKAGE*LIST**************/
struct TpackageList{
	vector <Tpackage> package;
	int n;
};

/************USER*REQUESTS************/
struct TuserRequest{
	double cost;
	double gflops;
	double ram;
	double disk;
	int timeMax;
	int nPackage;
    double alpha1, alpha2;
};

/**************************************PROCEDURE*DECLARATION***********************************************/

double calculate_time(unsigned long int ini, unsigned long int end);

void read_Instance(string instance, TpackageList* list, TuserRequest* userRequest);

//int optimizationModel (string instanceFile, TpackageList list, TuserRequest userRequest, double alpha1, double alpha2, int **solution);

bool compare_fo(Tpackage const& p1, Tpackage const& p2);

void heuristic(TpackageList* list, TuserRequest userRequest, double alpha1, double alpha2, int np, int *tm, int it, int** solution);

double max(double a1, double a2);

double calculate_cost_function(TpackageList* list, TuserRequest userRequest, double alpha1, double alpha2, int **solution, int tm);

int improved(double fant, double fnew);

int swap_1(TpackageList* list, TuserRequest userRequest,double alpha1, double alpha2, int **solution, int* tm, double* fant, double * fnew);

int swap_2(TpackageList* list, TuserRequest userRequest,double alpha1, double alpha2, int **solution, int* tm, double* fant, double * fnew);

void local_search(TpackageList* list, TuserRequest userRequest, double alpha1, double alpha2, int **solution, int* tm,  double* fnew, double * fant);

void copy_solution(TpackageList list, TuserRequest userRequest,int nl, int np, int** solution_best, int** solution);

//void setVariables(UFFProblem *prob, TpackageList list, TuserRequest userRequest, int** solution);

//void notSetVariables(UFFProblem *prob, TpackageList list, TuserRequest userRequest, int** solution);

#undef EXTERN
#endif
