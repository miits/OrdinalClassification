import os
import pandas as pd
import csv
from argparse import ArgumentParser


def count(path_to_file, dataset_name):
    df = pd.read_csv(path_to_file, sep=';')
    total = df.shape[0]
    count_df = df.groupby('type')['index'].count().reset_index(name='count')
    results = {'name': dataset_name}
    for index, row in count_df.iterrows():
        results[row['type'].lower()] = row['count'] / total * 100
    return results


def save_csv(data, fieldnames, csv_file_path):
    with open(csv_file_path, 'w', newline='') as file:
        writer = csv.DictWriter(file, delimiter=';', fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(data)


def analyze(path):
    union_knn_results = None
    union_kernel_results = None
    class_knn_results = None
    class_kernel_results = None
    for filename in os.listdir(path):
        path_to_file = os.path.join(path, filename)
        dataset_name = os.path.basename(os.path.normpath(path))
        if filename.endswith('knn.csv'):
            if filename.startswith('union'):
                union_knn_results = count(path_to_file, dataset_name)
            else:
                class_knn_results = count(path_to_file, dataset_name)
        elif filename.endswith('kernel.csv'):
            if filename.startswith('union'):
                union_kernel_results = count(path_to_file, dataset_name)
            else:
                class_kernel_results = count(path_to_file, dataset_name)
    return union_knn_results, union_kernel_results, class_knn_results, class_kernel_results


def make_stats(csv_path, results_path):
    union_knn_results = []
    union_kernel_results = []
    class_knn_results = []
    class_kernel_results = []
    for entry in os.scandir(csv_path):
        if entry.is_dir:
            path = os.path.join(csv_path, entry.name)
            union_knn_res, union_kernel_res, class_knn_res, class_kernel_res = analyze(path)
            union_knn_results.append(union_knn_res)
            union_kernel_results.append(union_kernel_res)
            class_knn_results.append(class_knn_res)
            class_kernel_results.append(class_kernel_res)
    fieldnames = ['name', 'safe', 'borderline', 'rare', 'outlier']
    save_csv(union_knn_results, fieldnames, os.path.join(results_path, 'union_knn.csv'))
    save_csv(union_kernel_results, fieldnames, os.path.join(results_path, 'union_kernel.csv'))
    save_csv(class_knn_results, fieldnames, os.path.join(results_path, 'class_knn.csv'))
    save_csv(class_kernel_results, fieldnames, os.path.join(results_path, 'class_kernel.csv'))


def main():
    parser = ArgumentParser()
    parser.add_argument("-c", "--csvpath", dest="csv",
                        help="path to csv containing examples labelling")
    parser.add_argument("-r", "--resultsdir", dest="results",
                        help="path to output directory")
    args = parser.parse_args()
    make_stats(args.csv, args.results)


if __name__ == "__main__":
    main()
